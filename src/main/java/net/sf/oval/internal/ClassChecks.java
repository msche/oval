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

import net.sf.oval.Check;
import net.sf.oval.CheckExclusion;
import net.sf.oval.exception.InvalidConfigurationException;
import net.sf.oval.guard.IsGuarded;
import net.sf.oval.guard.ParameterNameResolver;
import net.sf.oval.internal.util.ArrayUtils;
import net.sf.oval.internal.util.ReflectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * This class holds the instantiated checks for a single class.
 * <p/>
 * <b>Note:</b> For performance reasons the collections are made public (intended for read-access only).
 * Modifications to the collections should be done through the appropriate methods addXXX, removeXXX, clearXXX methods.
 *
 * @author Sebastian Thomschke
 */
public final class ClassChecks {
    private static final String GUARDING_MAY_NOT_BE_ACTIVATED_MESSAGE = //
            " Class does not implement IsGuarded interface. This indicates, " + //
                    "that constraints guarding may not activated for this class.";

    private static final Logger LOG = LoggerFactory.getLogger(ClassChecks.class);

    /**
     * checks on constructors' parameter values
     */
    public final Map<Constructor<?>, Map<Integer, ParameterChecks>> checksForConstructorParameters = new LinkedHashMap<>(2);

    /**
     * checks on fields' value
     */
    private final Map<Field, Set<Check>> checksForFields = new LinkedHashMap<>();

    /**
     * checks on methods' parameter values
     */
    public final Map<Method, Map<Integer, ParameterChecks>> checksForMethodParameters = new LinkedHashMap<>();

    /**
     * checks on methods' return value
     */
    public final Map<Method, Set<Check>> checksForMethodReturnValues = new LinkedHashMap<>();

    /**
     * compound constraints / object level invariants
     */
    public final Set<Check> checksForObject = new LinkedHashSet<>(2);

    public final Class<?> clazz;

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

    public final Set<AccessibleObject> methodsWithCheckInvariantsPost = new LinkedHashSet<>();

    public final Set<Method> methodsWithCheckInvariantsPre = new LinkedHashSet<>();

    private final ParameterNameResolver parameterNameResolver;

    /**
     * package constructor used by the Validator class
     *
     * @param clazz
     */
    public ClassChecks(final Class<?> clazz, final ParameterNameResolver parameterNameResolver) {
        LOG.debug("Initializing constraints configuration for class {}", clazz);

        this.clazz = clazz;
        this.parameterNameResolver = parameterNameResolver;
    }


    /**
     * Returns checks for specified Field
     *
     * @param field Field for which we want to retrieve checks
     * @return Set containing checks for specified field. If there are no checks for the specified field, the returned set will be empty.
     */
    public Set<Check> getChecks(Field field) {
        synchronized (checksForFields) {
            if (checksForFields.containsKey(field)) {
                return Collections.unmodifiableSet(checksForFields.get(field));
            } else {
                return new HashSet();
            }
        }
    }

    /**
     * Get checks for parameter of constructor.
     *
     * @param constructor Constructor for which we want to retrieve parameter checks
     * @param paramIndex  index of parameter for which we want to retrieve parameter checks
     * @return checks for specified constructor parameter.
     */
    private ParameterChecks getChecksOfConstructorParameter(final Constructor<?> constructor, final int paramIndex) {
        final int paramCount = constructor.getParameterTypes().length;

        if (paramIndex >= 0 && paramIndex < paramCount) {
            synchronized (checksForConstructorParameters) {
                // retrieve the currently registered checks for all parameters of the specified constructor
                Map<Integer, ParameterChecks> checksOfConstructorByParameter = checksForConstructorParameters.get(constructor);
                if (checksOfConstructorByParameter == null) {
                    checksOfConstructorByParameter = new LinkedHashMap<>(paramCount);
                    checksForConstructorParameters.put(constructor, checksOfConstructorByParameter);
                }

                // retrieve the checks for the specified parameter
                ParameterChecks checksOfConstructorParameter = checksOfConstructorByParameter.get(paramIndex);
                if (checksOfConstructorParameter == null) {
                    checksOfConstructorParameter = new ParameterChecks(constructor, paramIndex,
                            parameterNameResolver.getParameterNames(constructor)[paramIndex]);
                    checksOfConstructorByParameter.put(paramIndex, checksOfConstructorParameter);
                }

                return checksOfConstructorParameter;
            }
        } else {
            throw new InvalidConfigurationException("Parameter Index " + paramIndex + " is out of range (0-" + (paramCount - 1) + ")");
        }
    }

    /**
     * Get checks for parameter of method.
     *
     * @param method     Method for which we want to retrieve parameter checks.
     * @param paramIndex Index of parameter for which we want to retrieve parameter checks.
     * @return Checks for specified method parameter.
     */
    private ParameterChecks getChecksOfMethodParameter(final Method method, final int paramIndex) {
        final int paramCount = method.getParameterTypes().length;

        if (paramIndex >= 0 && paramIndex < paramCount) {
            synchronized (checksForMethodParameters) {
                // retrieve the currently registered checks for all parameters of the specified method
                Map<Integer, ParameterChecks> checksOfMethodByParameter = checksForMethodParameters.get(method);
                if (checksOfMethodByParameter == null) {
                    checksOfMethodByParameter = new LinkedHashMap<>(paramCount);
                    checksForMethodParameters.put(method, checksOfMethodByParameter);
                }

                // retrieve the checks for the specified parameter
                ParameterChecks checksOfMethodParameter = checksOfMethodByParameter.get(paramIndex);
                if (checksOfMethodParameter == null) {
                    checksOfMethodParameter = new ParameterChecks(method, paramIndex,
                            parameterNameResolver.getParameterNames(method)[paramIndex]);
                    checksOfMethodByParameter.put(paramIndex, checksOfMethodParameter);
                }

                return checksOfMethodParameter;
            }
        } else {
            throw new InvalidConfigurationException("Parameter index " + paramIndex + " is out of range (0-" + (paramCount - 1) + ")");
        }

    }

    /**
     * adds constraint checks to a constructor parameter
     *
     * @param constructor
     * @param parameterIndex
     * @param checks
     * @throws InvalidConfigurationException if the declaring class is not guarded by GuardAspect
     */
    public void addConstructorParameterChecks(final Constructor<?> constructor, final int parameterIndex, final Check... checks)
            throws InvalidConfigurationException {
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
    public void addConstructorParameterChecks(final Constructor<?> constructor, final int parameterIndex, final Collection<Check> checks)
            throws InvalidConfigurationException {
        if (!IsGuarded.class.isAssignableFrom(clazz))
            LOG.warn("Constructor parameter constraints may not be validated. {}", GUARDING_MAY_NOT_BE_ACTIVATED_MESSAGE);

        final ParameterChecks checksOfConstructorParameter = getChecksOfConstructorParameter(constructor, parameterIndex);
        checksOfConstructorParameter.addChecks(checks);

//        for (final Check check : checks)
//        {
//            checksOfConstructorParameter.checks.add(check);
//            if (check.getContext() == null) check.setContext(checksOfConstructorParameter.context);
//        }
    }

    /**
     * adds check constraints to a field
     *
     * @param field
     * @param checks
     */
    public void addFieldChecks(final Field field, final Check... checks) throws InvalidConfigurationException {
        addFieldChecks(field, ArrayUtils.asList(checks));
    }

    /**
     * adds check constraints to a field
     *
     * @param field
     * @param checks
     */
    public void addFieldChecks(final Field field, final Collection<Check> checks) throws InvalidConfigurationException {
        synchronized (checksForFields) {
            Set<Check> checksOfField = checksForFields.get(field);
            if (checksOfField == null) {
                checksOfField = new LinkedHashSet<>(2);
                checksForFields.put(field, checksOfField);
                if (ReflectionUtils.isStatic(field))
                    constrainedStaticFields.add(field);
                else
                    constrainedFields.add(field);
            }

            for (final Check check : checks) {
                checksOfField.add(check);
                if (check.getContext() == null) check.setContext(ContextCache.getFieldContext(field));
            }
        }
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
            throws InvalidConfigurationException {
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
            throws InvalidConfigurationException {
        if (!IsGuarded.class.isAssignableFrom(clazz))
            LOG.warn("Method parameter constraints may not be validated. {}", GUARDING_MAY_NOT_BE_ACTIVATED_MESSAGE);

        final ParameterChecks checksOfMethodParameter = getChecksOfMethodParameter(method, parameterIndex);
        checksOfMethodParameter.addChecks(checks);
//            for (final Check check : checks)
//            {
//                if (check.getContext() == null) check.setContext(checksOfMethodParameter.context);
//                checksOfMethodParameter.checks.add(check);
//            }
    }

    /**
     * adds constraint checks to a method's return value
     *
     * @param method
     * @param isInvariant determines if the return value should be checked when the object is validated, can be null
     * @param checks
     */
    public void addMethodReturnValueChecks(final Method method, final Boolean isInvariant, final Check... checks)
            throws InvalidConfigurationException {
        addMethodReturnValueChecks(method, isInvariant, ArrayUtils.asList(checks));
    }

    /**
     * adds constraint checks to a method's return value
     *
     * @param method
     * @param isInvariant determines if the return value should be checked when the object is validated, can be null
     * @param checks
     */
    public void addMethodReturnValueChecks(final Method method, final Boolean isInvariant, final Collection<Check> checks)
            throws InvalidConfigurationException {
        // ensure the method has a return type
        if (method.getReturnType() == Void.TYPE)
            throw new InvalidConfigurationException("Adding return value constraints for method " + method
                    + " is not possible. The method is declared as void and does not return any values.");

        final boolean hasParameters = method.getParameterTypes().length > 0;

        if (hasParameters && !IsGuarded.class.isAssignableFrom(clazz))
            LOG.warn("Method return value constraints may not be validated. {}", GUARDING_MAY_NOT_BE_ACTIVATED_MESSAGE);

        final boolean isInvariant2 = isInvariant == null ? constrainedMethods.contains(method) : isInvariant;

        if (!isInvariant2 && !IsGuarded.class.isAssignableFrom(clazz))
            LOG.warn("Method return value constraints may not be validated. {}", GUARDING_MAY_NOT_BE_ACTIVATED_MESSAGE);

        synchronized (checksForMethodReturnValues) {
            if (!hasParameters && isInvariant2) {
                if (ReflectionUtils.isStatic(method))
                    constrainedStaticMethods.add(method);
                else
                    constrainedMethods.add(method);
            } else if (ReflectionUtils.isStatic(method))
                constrainedStaticMethods.remove(method);
            else
                constrainedMethods.remove(method);

            Set<Check> methodChecks = checksForMethodReturnValues.get(method);
            if (methodChecks == null) {
                methodChecks = new LinkedHashSet<>(checks.size());
                checksForMethodReturnValues.put(method, methodChecks);
            }

            for (final Check check : checks) {
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
    public void addObjectChecks(final Check... checks) {
        addObjectChecks(ArrayUtils.asList(checks));
    }

    /**
     * adds check constraints on object level (invariants)
     *
     * @param checks
     */
    public void addObjectChecks(final Collection<Check> checks) {
        for (final Check check : checks) {
            if (check.getContext() == null) check.setContext(ContextCache.getClassContext(clazz));
        }

        synchronized (checksForObject) {
            checksForObject.addAll(checks);
        }
    }

    public synchronized void clear() {
        LOG.debug("Clearing all checks for class {}", clazz);

        checksForObject.clear();
        checksForConstructorParameters.clear();
        checksForFields.clear();
        checksForMethodReturnValues.clear();
        checksForMethodParameters.clear();
        constrainedFields.clear();
        constrainedStaticFields.clear();
        constrainedMethods.clear();
        constrainedStaticMethods.clear();
    }

    public void clearConstructorChecks(final Constructor<?> constructor) {
        clearConstructorParameterChecks(constructor);
    }

    public void clearConstructorParameterChecks(final Constructor<?> constructor) {
        synchronized (checksForConstructorParameters) {
            checksForConstructorParameters.remove(constructor);
        }
    }

    public void clearConstructorParameterChecks(final Constructor<?> constructor, final int parameterIndex) {
        synchronized (checksForConstructorParameters) {
            // retrieve the currently registered checks for all parameters of the specified method
            final Map<Integer, ParameterChecks> checksOfConstructorByParameter = checksForConstructorParameters.get(constructor);
            if (checksOfConstructorByParameter == null) return;

            // retrieve the checks for the specified parameter
            final ParameterChecks checksOfMethodParameter = checksOfConstructorByParameter.get(parameterIndex);
            if (checksOfMethodParameter == null) return;

            checksOfConstructorByParameter.remove(parameterIndex);
        }
    }

    public void clearFieldChecks(final Field field) {
        synchronized (checksForFields) {
            checksForFields.remove(field);
            constrainedFields.remove(field);
            constrainedStaticFields.remove(field);
        }
    }

    public synchronized void clearMethodChecks(final Method method) {
        clearMethodParameterChecks(method);
        clearMethodReturnValueChecks(method);
    }

    public void clearMethodParameterChecks(final Method method) {
        synchronized (checksForMethodParameters) {
            checksForMethodParameters.remove(method);
        }
    }

    public void clearMethodParameterChecks(final Method method, final int parameterIndex) {
        synchronized (checksForMethodParameters) {
            // retrieve the currently registered checks for all parameters of the specified method
            final Map<Integer, ParameterChecks> checksOfMethodByParameter = checksForMethodParameters.get(method);
            if (checksOfMethodByParameter == null) return;

            // retrieve the checks for the specified parameter
            final ParameterChecks checksOfMethodParameter = checksOfMethodByParameter.get(parameterIndex);
            if (checksOfMethodParameter == null) return;

            checksOfMethodByParameter.remove(parameterIndex);
        }
    }

    public void clearMethodReturnValueChecks(final Method method) {
        synchronized (checksForMethodReturnValues) {
            checksForMethodReturnValues.remove(method);
            constrainedMethods.remove(method);
            constrainedStaticMethods.remove(method);
        }
    }

    public void clearObjectChecks() {
        synchronized (checksForObject) {
            checksForObject.clear();
        }
    }

    public void removeConstructorParameterChecks(final Constructor<?> constructor, final int parameterIndex, final Check... checks) {
        synchronized (checksForConstructorParameters) {
            // retrieve the currently registered checks for all parameters of the specified method
            final Map<Integer, ParameterChecks> checksOfConstructorByParameter = checksForConstructorParameters.get(constructor);
            if (checksOfConstructorByParameter == null) return;

            // retrieve the checks for the specified parameter
            final ParameterChecks checksOfConstructorParameter = checksOfConstructorByParameter.get(parameterIndex);
            if (checksOfConstructorParameter == null) return;

            for (final Check check : checks)
                checksOfConstructorParameter.removeCheck(check);

            if (checksOfConstructorParameter.hasChecks()) checksOfConstructorByParameter.remove(parameterIndex);
        }
    }

    public void removeFieldChecks(final Field field, final Check... checks) {
        synchronized (checksForFields) {
            final Set<Check> checksOfField = checksForFields.get(field);

            if (checksOfField == null) return;

            for (final Check check : checks)
                checksOfField.remove(check);

            if (checksOfField.size() == 0) {
                checksForFields.remove(field);
                constrainedFields.remove(field);
                constrainedStaticFields.remove(field);
            }
        }
    }

    public void removeMethodParameterChecks(final Method method, final int parameterIndex, final Check... checks)
            throws InvalidConfigurationException {
        if (parameterIndex < 0 || parameterIndex > method.getParameterTypes().length)
            throw new InvalidConfigurationException("ParameterIndex is out of range");

        synchronized (checksForMethodParameters) {
            // retrieve the currently registered checks for all parameters of the specified method
            final Map<Integer, ParameterChecks> checksOfMethodByParameter = checksForMethodParameters.get(method);
            if (checksOfMethodByParameter == null) return;

            // retrieve the checks for the specified parameter
            final ParameterChecks checksOfMethodParameter = checksOfMethodByParameter.get(parameterIndex);
            if (checksOfMethodParameter == null) return;

            for (final Check check : checks)
                checksOfMethodParameter.removeCheck(check);

            if (checksOfMethodParameter.hasChecks()) checksOfMethodByParameter.remove(parameterIndex);
        }
    }

    public void removeMethodReturnValueChecks(final Method method, final Check... checks) {
        synchronized (checksForMethodReturnValues) {
            final Set<Check> checksOfMethod = checksForMethodReturnValues.get(method);

            if (checksOfMethod == null) return;

            for (final Check check : checks)
                checksOfMethod.remove(check);

            if (checksOfMethod.size() == 0) {
                checksForMethodReturnValues.remove(method);
                constrainedMethods.remove(method);
                constrainedStaticMethods.remove(method);
            }
        }
    }

    public void removeObjectChecks(final Check... checks) {
        synchronized (checksForObject) {
            for (final Check check : checks)
                checksForObject.remove(check);
        }
    }
}
