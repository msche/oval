/*******************************************************************************
 * Portions created by Sebastian Thomschke are copyright (c) 2005-2013 Sebastian
 * Thomschke.
 *
 * All Rights Reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Sebastian Thomschke - initial implementation.
 *******************************************************************************/
package net.sf.oval.internal;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import net.sf.oval.Check;
import net.sf.oval.CheckExclusion;
import net.sf.oval.exception.InvalidConfigurationException;
import net.sf.oval.guard.IsGuarded;
import net.sf.oval.guard.ParameterNameResolver;
import net.sf.oval.guard.PostCheck;
import net.sf.oval.guard.PreCheck;
import net.sf.oval.internal.util.ArrayUtils;
import net.sf.oval.internal.util.ReflectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class holds the instantiated checks for a single class.
 *
 * <b>Note:</b> For performance reasons the collections are made public (intended for read-access only).
 * Modifications to the collections should be done through the appropriate methods addXXX, removeXXX, clearXXX methods.
 *
 * @author Sebastian Thomschke
 */
public final class ClassChecks
{
	private static final String GUARDING_MAY_NOT_BE_ACTIVATED_MESSAGE = //
	" Class does not implement IsGuarded interface. This indicates, " + //
			"that constraints guarding may not activated for this class.";

	private static final Logger LOG = LoggerFactory.getLogger(ClassChecks.class);

	/**
	 * checks on constructors' parameter values
	 */
	public final Map<Constructor< ? >, Map<Integer, ParameterChecks>> checksForConstructorParameters = new LinkedHashMap<>(2);

	/**
	 * checks on fields' value
	 */
	public final Map<Field, Set<Check>> checksForFields = new LinkedHashMap<>();

	/**
	 * checks on methods' parameter values
	 */
	public final Map<Method, Map<Integer, ParameterChecks>> checksForMethodParameters = new LinkedHashMap<>();

	/**
	 * checks on methods' return value
	 */
	public final Map<Method, Set<Check>> checksForMethodReturnValues = new LinkedHashMap<>();

	public final Map<Method, Set<PostCheck>> checksForMethodsPostExcecution = new LinkedHashMap<>();

	public final Map<Method, Set<PreCheck>> checksForMethodsPreExecution = new LinkedHashMap<>();

	/**
	 * compound constraints / object level invariants
	 */
	public final Set<Check> checksForObject = new LinkedHashSet<>(2);

	public final Class< ? > clazz;

	/**
	 * all non-static fields that have value constraints.
	 * Validator loops over this set during validation.
	 */
	public final Set<Field> constrainedFields = new LinkedHashSet<>();

	/**
	 * all non-static non-void, non-parameterized methods marked as invariant that have return value constraints.
	 * Validator loops over this set during validation.
	 */
	public final Set<Method> constrainedMethods = new LinkedHashSet<>();

	/**
	 * all non-static fields that have value constraints.
	 * Validator loops over this set during validation.
	 */
	public final Set<Field> constrainedStaticFields = new LinkedHashSet<>();

	/**
	 * all static non-void, non-parameterized methods marked as invariant that have return value constraints.
	 * Validator loops over this set during validation.
	 */
	public final Set<Method> constrainedStaticMethods = new LinkedHashSet<>();

	public boolean isCheckInvariants;

	public final Set<AccessibleObject> methodsWithCheckInvariantsPost = new LinkedHashSet<>();

	public final Set<Method> methodsWithCheckInvariantsPre = new LinkedHashSet<>();

	private final ParameterNameResolver parameterNameResolver;

	/**
	 * package constructor used by the Validator class
	 *
	 * @param clazz
	 */
	public ClassChecks(final Class< ? > clazz, final ParameterNameResolver parameterNameResolver)
	{
		LOG.debug("Initializing constraints configuration for class {}", clazz);

		this.clazz = clazz;
		this.parameterNameResolver = parameterNameResolver;
	}



	private ParameterChecks _getChecksOfConstructorParameter(final Constructor< ? > ctor, final int paramIndex)
	{
		final int paramCount = ctor.getParameterTypes().length;

		if (paramIndex < 0 || paramIndex >= paramCount)
			throw new InvalidConfigurationException("Parameter Index " + paramIndex + " is out of range (0-" + (paramCount - 1) + ")");

		synchronized (checksForConstructorParameters)
		{
			// retrieve the currently registered checks for all parameters of the specified constructor
			Map<Integer, ParameterChecks> checksOfConstructorByParameter = checksForConstructorParameters.get(ctor);
			if (checksOfConstructorByParameter == null)
			{
				checksOfConstructorByParameter = new LinkedHashMap<>(paramCount);
				checksForConstructorParameters.put(ctor, checksOfConstructorByParameter);
			}

			// retrieve the checks for the specified parameter
			ParameterChecks checksOfConstructorParameter = checksOfConstructorByParameter.get(paramIndex);
			if (checksOfConstructorParameter == null)
			{
				checksOfConstructorParameter = new ParameterChecks(ctor, paramIndex,
						parameterNameResolver.getParameterNames(ctor)[paramIndex]);
				checksOfConstructorByParameter.put(paramIndex, checksOfConstructorParameter);
			}

			return checksOfConstructorParameter;
		}
	}

	private ParameterChecks _getChecksOfMethodParameter(final Method method, final int paramIndex)
	{
		final int paramCount = method.getParameterTypes().length;

		if (paramIndex < 0 || paramIndex >= paramCount)
			throw new InvalidConfigurationException("Parameter index " + paramIndex + " is out of range (0-" + (paramCount - 1) + ")");

		synchronized (checksForMethodParameters)
		{
			// retrieve the currently registered checks for all parameters of the specified method
			Map<Integer, ParameterChecks> checksOfMethodByParameter = checksForMethodParameters.get(method);
			if (checksOfMethodByParameter == null)
			{
				checksOfMethodByParameter = new LinkedHashMap<>(paramCount);
				checksForMethodParameters.put(method, checksOfMethodByParameter);
			}

			// retrieve the checks for the specified parameter
			ParameterChecks checksOfMethodParameter = checksOfMethodByParameter.get(paramIndex);
			if (checksOfMethodParameter == null)
			{
				checksOfMethodParameter = new ParameterChecks(method, paramIndex,
						parameterNameResolver.getParameterNames(method)[paramIndex]);
				checksOfMethodByParameter.put(paramIndex, checksOfMethodParameter);
			}

			return checksOfMethodParameter;
		}
	}

	/**
	 * adds constraint check exclusions to a constructor parameter
	 *
	 * @param constructor
	 * @param parameterIndex
	 * @param exclusions
	 * @throws InvalidConfigurationException if the declaring class is not guarded by GuardAspect
	 */
	public void addConstructorParameterCheckExclusions(final Constructor< ? > constructor, final int parameterIndex,
			final CheckExclusion... exclusions) throws InvalidConfigurationException
	{
        addConstructorParameterCheckExclusions(constructor, parameterIndex, ArrayUtils.asList(exclusions));
	}

	/**
	 * adds constraint check exclusions to a constructor parameter
	 *
	 * @param constructor
	 * @param parameterIndex
	 * @param exclusions
	 * @throws InvalidConfigurationException if the declaring class is not guarded by GuardAspect
	 */
	public void addConstructorParameterCheckExclusions(final Constructor< ? > constructor, final int parameterIndex,
			final Collection<CheckExclusion> exclusions) throws InvalidConfigurationException
	{
        final ParameterChecks checksOfConstructorParameter = _getChecksOfConstructorParameter(constructor, parameterIndex);
        checksOfConstructorParameter.checkExclusions.addAll(exclusions);
	}

	/**
	 * adds constraint checks to a constructor parameter
	 *
	 * @param constructor
	 * @param parameterIndex
	 * @param checks
	 * @throws InvalidConfigurationException if the declaring class is not guarded by GuardAspect
	 */
	public void addConstructorParameterChecks(final Constructor< ? > constructor, final int parameterIndex, final Check... checks)
			throws InvalidConfigurationException
	{
        addConstructorParameterChecks(constructor, parameterIndex, ArrayUtils.asList(checks));
	}

	/**
	 * adds constraint checks to a constructor parameter
	 *
	 * @param constructor
	 * @param parameterIndex
	 * @param checks
	 * @throws InvalidConfigurationException if the declaring class is not guarded by GuardAspect
	 */
	public void addConstructorParameterChecks(final Constructor< ? > constructor, final int parameterIndex, final Collection<Check> checks)
			throws InvalidConfigurationException
	{
        if (!IsGuarded.class.isAssignableFrom(clazz))
            LOG.warn("Constructor parameter constraints may not be validated. {}", GUARDING_MAY_NOT_BE_ACTIVATED_MESSAGE);

        final ParameterChecks checksOfConstructorParameter = _getChecksOfConstructorParameter(constructor, parameterIndex);

        for (final Check check : checks)
        {
            checksOfConstructorParameter.checks.add(check);
            if (check.getContext() == null) check.setContext(checksOfConstructorParameter.context);
        }
	}

	/**
	 * adds check constraints to a field
	 *
	 * @param field
	 * @param checks
	 */
	public void addFieldChecks(final Field field, final Check... checks) throws InvalidConfigurationException
	{
		addFieldChecks(field, ArrayUtils.asList(checks));
	}

	/**
	 * adds check constraints to a field
	 *
	 * @param field
	 * @param checks
	 */
	public void addFieldChecks(final Field field, final Collection<Check> checks) throws InvalidConfigurationException
	{
        synchronized (checksForFields)
        {
            Set<Check> checksOfField = checksForFields.get(field);
            if (checksOfField == null)
            {
                checksOfField = new LinkedHashSet<>(2);
                checksForFields.put(field, checksOfField);
                if (ReflectionUtils.isStatic(field))
                    constrainedStaticFields.add(field);
                else
                    constrainedFields.add(field);
            }

                for (final Check check : checks)
                {
                    checksOfField.add(check);
                    if (check.getContext() == null) check.setContext(ContextCache.getFieldContext(field));
                }
        }
	}

	/**
	 * adds constraint check exclusions to a method parameter
	 *
	 * @param method
	 * @param parameterIndex
	 * @param exclusions
	 * @throws InvalidConfigurationException if the declaring class is not guarded by GuardAspect
	 */
	public void addMethodParameterCheckExclusions(final Method method, final int parameterIndex, final CheckExclusion... exclusions)
			throws InvalidConfigurationException
	{
		addMethodParameterCheckExclusions(method, parameterIndex, ArrayUtils.asList(exclusions));
	}

	/**
	 * adds constraint check exclusions to a method parameter
	 *
	 * @param method
	 * @param parameterIndex
	 * @param exclusions
	 * @throws InvalidConfigurationException if the declaring class is not guarded by GuardAspect
	 */
	public void addMethodParameterCheckExclusions(final Method method, final int parameterIndex, final Collection<CheckExclusion> exclusions)
			throws InvalidConfigurationException
	{
        final ParameterChecks checksOfMethodParameter = _getChecksOfMethodParameter(method, parameterIndex);
            checksOfMethodParameter.checkExclusions.addAll(exclusions);
	}

	/**
	 * adds constraint checks to a method parameter
	 *
	 * @param method
	 * @param parameterIndex
	 * @param checks
	 * @throws InvalidConfigurationException if the declaring class is not guarded by GuardAspect
	 */
	public void addMethodParameterChecks(final Method method, final int parameterIndex, final Check... checks)
			throws InvalidConfigurationException
	{
		addMethodParameterChecks(method, parameterIndex, ArrayUtils.asList(checks));
	}

	/**
	 * adds constraint checks to a method parameter
	 *
	 * @param method
	 * @param parameterIndex
	 * @param checks
	 * @throws InvalidConfigurationException if the declaring class is not guarded by GuardAspect
	 */
	public void addMethodParameterChecks(final Method method, final int parameterIndex, final Collection<Check> checks)
			throws InvalidConfigurationException
	{
        if (!IsGuarded.class.isAssignableFrom(clazz))
            LOG.warn("Method parameter constraints may not be validated. {}", GUARDING_MAY_NOT_BE_ACTIVATED_MESSAGE);

        final ParameterChecks checksOfMethodParameter = _getChecksOfMethodParameter(method, parameterIndex);

            for (final Check check : checks)
            {
                if (check.getContext() == null) check.setContext(checksOfMethodParameter.context);
                checksOfMethodParameter.checks.add(check);
            }
	}

	/**
	 * adds constraint checks to a method's return value
	 * @param method
	 * @param checks
	 * @throws InvalidConfigurationException if the declaring class is not guarded by GuardAspect
	 */
	public void addMethodPostChecks(final Method method, final Collection<PostCheck> checks) throws InvalidConfigurationException
	{
        if (!IsGuarded.class.isAssignableFrom(clazz))
            LOG.warn("Method post-conditions may not be validated. {}", GUARDING_MAY_NOT_BE_ACTIVATED_MESSAGE);

        synchronized (checksForMethodsPostExcecution)
        {
            Set<PostCheck> postChecks = checksForMethodsPostExcecution.get(method);
            if (postChecks == null)
            {
                postChecks = new LinkedHashSet<>(2);
                checksForMethodsPostExcecution.put(method, postChecks);
            }

                for (final PostCheck check : checks)
                {
                    postChecks.add(check);
                    if (check.getContext() == null) check.setContext(ContextCache.getMethodExitContext(method));
                }
        }
	}

	/**
	 * adds constraint checks to a method's return value
	 * @param method
	 * @param checks
	 * @throws InvalidConfigurationException if the declaring class is not guarded by GuardAspect
	 */
	public void addMethodPostChecks(final Method method, final PostCheck... checks) throws InvalidConfigurationException
	{
		addMethodPostChecks(method, ArrayUtils.asList(checks));
	}

	/**
	 * @param method
	 * @param checks
	 * @throws InvalidConfigurationException if the declaring class is not guarded by GuardAspect
	 */
	public void addMethodPreChecks(final Method method, final Collection<PreCheck> checks) throws InvalidConfigurationException
	{
        if (!IsGuarded.class.isAssignableFrom(clazz))
            LOG.warn("Method pre-conditions may not be validated. {}", GUARDING_MAY_NOT_BE_ACTIVATED_MESSAGE);

        synchronized (checksForMethodsPreExecution)
        {
            Set<PreCheck> preChecks = checksForMethodsPreExecution.get(method);
            if (preChecks == null)
            {
                preChecks = new LinkedHashSet<>(2);
                checksForMethodsPreExecution.put(method, preChecks);
            }

                for (final PreCheck check : checks)
                {
                    preChecks.add(check);
                    if (check.getContext() == null) check.setContext(ContextCache.getMethodEntryContext(method));
                }
        }
	}

	/**
	 * @param method
	 * @param checks
	 * @throws InvalidConfigurationException if the declaring class is not guarded by GuardAspect
	 */
	public void addMethodPreChecks(final Method method, final PreCheck... checks) throws InvalidConfigurationException
	{
        addMethodPreChecks(method, ArrayUtils.asList(checks));
	}

	/**
	 * adds constraint checks to a method's return value
	 * @param method
	 * @param isInvariant determines if the return value should be checked when the object is validated, can be null
	 * @param checks
	 */
	public void addMethodReturnValueChecks(final Method method, final Boolean isInvariant, final Check... checks)
			throws InvalidConfigurationException
	{
		addMethodReturnValueChecks(method, isInvariant, ArrayUtils.asList(checks));
	}

	/**
	 * adds constraint checks to a method's return value
	 * @param method
	 * @param isInvariant determines if the return value should be checked when the object is validated, can be null
	 * @param checks
	 */
	public void addMethodReturnValueChecks(final Method method, final Boolean isInvariant, final Collection<Check> checks)
			throws InvalidConfigurationException
	{
		// ensure the method has a return type
		if (method.getReturnType() == Void.TYPE)
			throw new InvalidConfigurationException("Adding return value constraints for method " + method
					+ " is not possible. The method is declared as void and does not return any values.");

		if (ReflectionUtils.isVoidMethod(method))
			throw new InvalidConfigurationException("Cannot apply method return value constraints for void method " + method);

		final boolean hasParameters = method.getParameterTypes().length > 0;

		if (hasParameters && !IsGuarded.class.isAssignableFrom(clazz))
			LOG.warn("Method return value constraints may not be validated. {}", GUARDING_MAY_NOT_BE_ACTIVATED_MESSAGE);

		final boolean isInvariant2 = isInvariant == null ? constrainedMethods.contains(method) : isInvariant;

		if (!isInvariant2 && !IsGuarded.class.isAssignableFrom(clazz))
			LOG.warn("Method return value constraints may not be validated. {}", GUARDING_MAY_NOT_BE_ACTIVATED_MESSAGE);

		synchronized (checksForMethodReturnValues)
		{
			if (!hasParameters && isInvariant2)
			{
				if (ReflectionUtils.isStatic(method))
					constrainedStaticMethods.add(method);
				else
					constrainedMethods.add(method);
			}
			else if (ReflectionUtils.isStatic(method))
				constrainedStaticMethods.remove(method);
			else
				constrainedMethods.remove(method);

			Set<Check> methodChecks = checksForMethodReturnValues.get(method);
			if (methodChecks == null)
			{
				methodChecks = new LinkedHashSet<>(2);
				checksForMethodReturnValues.put(method, methodChecks);
			}

				for (final Check check : checks)
				{
					methodChecks.add(check);
					if (check.getContext() == null) check.setContext(ContextCache.getMethodReturnValueContext(method));
				}
		}
	}

	/**
	 * adds check constraints on object level (invariants)
	 *
	 * @param checks
	 */
	public void addObjectChecks(final Check... checks)
	{
        addObjectChecks(ArrayUtils.asList(checks));
//		synchronized (checksForObject)
//		{
//			for (final Check check : checks)
//			{
//				if (check.getContext() == null) check.setContext(ContextCache.getClassContext(clazz));
//				checksForObject.add(check);
//			}
//		}
	}

	/**
	 * adds check constraints on object level (invariants)
	 *
	 * @param checks
	 */
	public void addObjectChecks(final Collection<Check> checks)
	{
		synchronized (checksForObject)
		{
			for (final Check check : checks)
			{
				if (check.getContext() == null) check.setContext(ContextCache.getClassContext(clazz));
				checksForObject.add(check);
			}
		}
	}

	public synchronized void clear()
	{
		LOG.debug("Clearing all checks for class {}", clazz);

		checksForObject.clear();
		checksForMethodsPostExcecution.clear();
		checksForMethodsPreExecution.clear();
		checksForConstructorParameters.clear();
		checksForFields.clear();
		checksForMethodReturnValues.clear();
		checksForMethodParameters.clear();
		constrainedFields.clear();
		constrainedStaticFields.clear();
		constrainedMethods.clear();
		constrainedStaticMethods.clear();
	}

	public void clearConstructorChecks(final Constructor< ? > constructor)
	{
		clearConstructorParameterChecks(constructor);
	}

	public void clearConstructorParameterChecks(final Constructor< ? > constructor)
	{
		synchronized (checksForConstructorParameters)
		{
			checksForConstructorParameters.remove(constructor);
		}
	}

	public void clearConstructorParameterChecks(final Constructor< ? > constructor, final int parameterIndex)
	{
		synchronized (checksForConstructorParameters)
		{
			// retrieve the currently registered checks for all parameters of the specified method
			final Map<Integer, ParameterChecks> checksOfConstructorByParameter = checksForConstructorParameters.get(constructor);
			if (checksOfConstructorByParameter == null) return;

			// retrieve the checks for the specified parameter
			final ParameterChecks checksOfMethodParameter = checksOfConstructorByParameter.get(parameterIndex);
			if (checksOfMethodParameter == null) return;

			checksOfConstructorByParameter.remove(parameterIndex);
		}
	}

	public void clearFieldChecks(final Field field)
	{
		synchronized (checksForFields)
		{
			checksForFields.remove(field);
			constrainedFields.remove(field);
			constrainedStaticFields.remove(field);
		}
	}

	public synchronized void clearMethodChecks(final Method method)
	{
		clearMethodParameterChecks(method);
		clearMethodReturnValueChecks(method);
		clearMethodPreChecks(method);
		clearMethodPostChecks(method);
	}

	public void clearMethodParameterChecks(final Method method)
	{
		synchronized (checksForMethodParameters)
		{
			checksForMethodParameters.remove(method);
		}
	}

	public void clearMethodParameterChecks(final Method method, final int parameterIndex)
	{
		synchronized (checksForMethodParameters)
		{
			// retrieve the currently registered checks for all parameters of the specified method
			final Map<Integer, ParameterChecks> checksOfMethodByParameter = checksForMethodParameters.get(method);
			if (checksOfMethodByParameter == null) return;

			// retrieve the checks for the specified parameter
			final ParameterChecks checksOfMethodParameter = checksOfMethodByParameter.get(parameterIndex);
			if (checksOfMethodParameter == null) return;

			checksOfMethodByParameter.remove(parameterIndex);
		}
	}

	public void clearMethodPostChecks(final Method method)
	{
		synchronized (checksForMethodsPostExcecution)
		{
			checksForMethodsPostExcecution.remove(method);
		}
	}

	public void clearMethodPreChecks(final Method method)
	{
		synchronized (checksForMethodsPreExecution)
		{
			checksForMethodsPreExecution.remove(method);
		}
	}

	public void clearMethodReturnValueChecks(final Method method)
	{
		synchronized (checksForMethodReturnValues)
		{
			checksForMethodReturnValues.remove(method);
			constrainedMethods.remove(method);
			constrainedStaticMethods.remove(method);
		}
	}

	public void clearObjectChecks()
	{
		synchronized (checksForObject)
		{
			checksForObject.clear();
		}
	}

	public void removeConstructorParameterCheckExclusions(final Constructor< ? > constructor, final int parameterIndex,
			final CheckExclusion... exclusions)
	{
		synchronized (checksForConstructorParameters)
		{
			// retrieve the currently registered checks for all parameters of the specified method
			final Map<Integer, ParameterChecks> checksOfConstructorByParameter = checksForConstructorParameters.get(constructor);
			if (checksOfConstructorByParameter == null) return;

			// retrieve the checks for the specified parameter
			final ParameterChecks checksOfConstructorParameter = checksOfConstructorByParameter.get(parameterIndex);
			if (checksOfConstructorParameter == null) return;

			for (final CheckExclusion exclusion : exclusions)
				checksOfConstructorParameter.checkExclusions.remove(exclusion);

			if (checksOfConstructorParameter.isEmpty()) checksOfConstructorByParameter.remove(parameterIndex);
		}
	}

	public void removeConstructorParameterChecks(final Constructor< ? > constructor, final int parameterIndex, final Check... checks)
	{
		synchronized (checksForConstructorParameters)
		{
			// retrieve the currently registered checks for all parameters of the specified method
			final Map<Integer, ParameterChecks> checksOfConstructorByParameter = checksForConstructorParameters.get(constructor);
			if (checksOfConstructorByParameter == null) return;

			// retrieve the checks for the specified parameter
			final ParameterChecks checksOfConstructorParameter = checksOfConstructorByParameter.get(parameterIndex);
			if (checksOfConstructorParameter == null) return;

			for (final Check check : checks)
				checksOfConstructorParameter.checks.remove(check);

			if (checksOfConstructorParameter.isEmpty()) checksOfConstructorByParameter.remove(parameterIndex);
		}
	}

	public void removeFieldChecks(final Field field, final Check... checks)
	{
		synchronized (checksForFields)
		{
			final Set<Check> checksOfField = checksForFields.get(field);

			if (checksOfField == null) return;

			for (final Check check : checks)
				checksOfField.remove(check);

			if (checksOfField.size() == 0)
			{
				checksForFields.remove(field);
				constrainedFields.remove(field);
				constrainedStaticFields.remove(field);
			}
		}
	}

	public void removeMethodParameterCheckExclusions(final Method method, final int parameterIndex, final CheckExclusion... exclusions)
	{
		if (parameterIndex < 0 || parameterIndex > method.getParameterTypes().length)
			throw new InvalidConfigurationException("ParameterIndex is out of range");

		synchronized (checksForMethodParameters)
		{
			// retrieve the currently registered checks for all parameters of the specified method
			final Map<Integer, ParameterChecks> checksOfMethodByParameter = checksForMethodParameters.get(method);
			if (checksOfMethodByParameter == null) return;

			// retrieve the checks for the specified parameter
			final ParameterChecks checksOfMethodParameter = checksOfMethodByParameter.get(parameterIndex);
			if (checksOfMethodParameter == null) return;

			for (final CheckExclusion exclusion : exclusions)
				checksOfMethodParameter.checkExclusions.remove(exclusion);

			if (checksOfMethodParameter.isEmpty()) checksOfMethodByParameter.remove(parameterIndex);
		}
	}

	public void removeMethodParameterChecks(final Method method, final int parameterIndex, final Check... checks)
			throws InvalidConfigurationException
	{
		if (parameterIndex < 0 || parameterIndex > method.getParameterTypes().length)
			throw new InvalidConfigurationException("ParameterIndex is out of range");

		synchronized (checksForMethodParameters)
		{
			// retrieve the currently registered checks for all parameters of the specified method
			final Map<Integer, ParameterChecks> checksOfMethodByParameter = checksForMethodParameters.get(method);
			if (checksOfMethodByParameter == null) return;

			// retrieve the checks for the specified parameter
			final ParameterChecks checksOfMethodParameter = checksOfMethodByParameter.get(parameterIndex);
			if (checksOfMethodParameter == null) return;

			for (final Check check : checks)
				checksOfMethodParameter.checks.remove(check);

			if (checksOfMethodParameter.isEmpty()) checksOfMethodByParameter.remove(parameterIndex);
		}
	}

	public void removeMethodPostChecks(final Method method, final PostCheck... checks)
	{
		synchronized (checksForMethodsPostExcecution)
		{
			final Set<PostCheck> checksforMethod = checksForMethodsPostExcecution.get(method);

			if (checksforMethod == null) return;

			for (final PostCheck check : checks)
				checksforMethod.remove(check);

			if (checksforMethod.size() == 0) checksForMethodsPostExcecution.remove(method);
		}
	}

	public void removeMethodPreChecks(final Method method, final PreCheck... checks)
	{
		synchronized (checksForMethodsPreExecution)
		{
			final Set<PreCheck> checksforMethod = checksForMethodsPreExecution.get(method);

			if (checksforMethod == null) return;

			for (final PreCheck check : checks)
				checksforMethod.remove(check);

			if (checksforMethod.size() == 0) checksForMethodsPreExecution.remove(method);
		}
	}

	public void removeMethodReturnValueChecks(final Method method, final Check... checks)
	{
		synchronized (checksForMethodReturnValues)
		{
			final Set<Check> checksOfMethod = checksForMethodReturnValues.get(method);

			if (checksOfMethod == null) return;

			for (final Check check : checks)
				checksOfMethod.remove(check);

			if (checksOfMethod.size() == 0)
			{
				checksForMethodReturnValues.remove(method);
				constrainedMethods.remove(method);
				constrainedStaticMethods.remove(method);
			}
		}
	}

	public void removeObjectChecks(final Check... checks)
	{
		synchronized (checksForObject)
		{
			for (final Check check : checks)
				checksForObject.remove(check);
		}
	}
}
