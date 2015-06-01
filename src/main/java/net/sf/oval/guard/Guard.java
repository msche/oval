/*******************************************************************************
 * Portions created by Sebastian Thomschke are copyright (c) 2005-2014 Sebastian
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
package net.sf.oval.guard;

import net.sf.oval.Check;
import net.sf.oval.CheckExclusion;
import net.sf.oval.ConstraintViolation;
import net.sf.oval.Validator;
import net.sf.oval.configuration.Configurer;
import net.sf.oval.context.ConstructorParameterContext;
import net.sf.oval.context.MethodEntryContext;
import net.sf.oval.context.MethodExitContext;
import net.sf.oval.context.MethodParameterContext;
import net.sf.oval.context.MethodReturnValueContext;
import net.sf.oval.context.OValContext;
import net.sf.oval.exception.ConstraintsViolatedException;
import net.sf.oval.exception.InvalidConfigurationException;
import net.sf.oval.exception.OValException;
import net.sf.oval.exception.ValidationFailedException;
import net.sf.oval.expression.ExpressionLanguage;
import net.sf.oval.internal.ClassChecks;
import net.sf.oval.internal.ContextCache;
import net.sf.oval.internal.ParameterChecks;
import net.sf.oval.internal.util.ArrayUtils;
import net.sf.oval.internal.util.Assert;
import net.sf.oval.internal.util.IdentitySet;
import net.sf.oval.internal.util.Invocable;
import net.sf.oval.internal.util.ReflectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

/**
 * Extended version of the validator to realize programming by contract.
 *
 * @author msche
 */
public class Guard extends Validator
{

	private static final Logger LOG = LoggerFactory.getLogger(Guard.class);

	/**
	 * string based on validated object hashcode + method hashcode for currently validated method return values
	 */
	private static final List<String> currentlyCheckingMethodReturnValues = new ArrayList<>();

	private boolean isActivated = true;
	private boolean isInvariantsEnabled = true;

	/**
	 * Flag that indicates if any listeners were registered at any time. Used for improved performance.
	 */
	private boolean isListenersFeatureUsed = false;
	private final Set<ConstraintsViolatedListener> listeners = new IdentitySet<ConstraintsViolatedListener>(4);
	private final Map<Class< ? >, Set<ConstraintsViolatedListener>> listenersByClass = new WeakHashMap<Class< ? >, Set<ConstraintsViolatedListener>>(4);
	private final Map<Object, Set<ConstraintsViolatedListener>> listenersByObject = new WeakHashMap<Object, Set<ConstraintsViolatedListener>>(4);

	/**
	 * Constructs a new guard object and uses a new instance of AnnotationsConfigurer
	 */
	public Guard()
	{
		super();
	}

	public Guard(final Collection<Configurer> configurers)
	{
		super(configurers);
	}

	public Guard(final Configurer... configurers)
	{
		super(configurers);
	}

	private void _validateParameterChecks(final ParameterChecks parameterChecks, final Object validatedObject, final Object valueToValidate,
			final OValContext context, final List<ConstraintViolation> violations)
	{
		// check the constraints
		for (final Check check : parameterChecks.getChecks())
		{
			checkConstraint(violations, check, validatedObject, valueToValidate, context, null, false);
		}
	}

	/**
	 * Registers constraint checks for the given constructor parameter
	 *
	 * @param ctor
	 * @param paramIndex
	 * @param checks
	 * @throws IllegalArgumentException if <code>constructor == null</code> or <code>checks == null</code> or checks is
	 *             empty
	 * @throws InvalidConfigurationException if the declaring class is not guarded or the parameterIndex is out of range
	 */
	public void addChecks(final Constructor< ? > ctor, final int paramIndex, final Check... checks) throws IllegalArgumentException,
			InvalidConfigurationException
	{
		Assert.argumentNotNull("ctor", ctor);
		Assert.argumentNotEmpty("checks", checks);

		getClassChecks(ctor.getDeclaringClass()).addConstructorParameterChecks(ctor, paramIndex, checks);
	}

	/**
	 * Registers constraint checks for the given method's return value
	 *
	 * @param method
	 * @param checks
	 * @throws IllegalArgumentException if <code>getter == null</code> or <code>checks == null</code> or checks is empty
	 * @throws InvalidConfigurationException if method does not declare a return type (void), or the declaring class is
	 *             not guarded
	 */
	@Override
	public void addChecks(final Method method, final Check... checks) throws IllegalArgumentException, InvalidConfigurationException
	{
		Assert.argumentNotNull("method", method);
		Assert.argumentNotEmpty("checks", checks);

		getClassChecks(method.getDeclaringClass()).addMethodReturnValueChecks(method, null, checks);
	}

	/**
	 * Registers constraint checks for the given method parameter
	 *
	 * @param method
	 * @param paramIndex
	 * @param checks
	 * @throws IllegalArgumentException if <code>method == null</code> or <code>checks == null</code> or checks is empty
	 * @throws InvalidConfigurationException if the declaring class is not guarded or the parameterIndex is out of range
	 */
	public void addChecks(final Method method, final int paramIndex, final Check... checks) throws IllegalArgumentException,
			InvalidConfigurationException
	{
		Assert.argumentNotNull("method", method);
		Assert.argumentNotEmpty("checks", checks);

		getClassChecks(method.getDeclaringClass()).addMethodParameterChecks(method, paramIndex, checks);
	}

	/**
	 * Registers the given listener for <b>all</b> thrown ConstraintViolationExceptions
	 *
	 * @param listener the listener to register
	 * @return <code>true</code> if the listener was not yet registered
	 * @throws IllegalArgumentException if <code>listener == null</code>
	 */
	public boolean addListener(final ConstraintsViolatedListener listener) throws IllegalArgumentException
	{
		Assert.argumentNotNull("listener", listener);

		isListenersFeatureUsed = true;
		return listeners.add(listener);
	}

	/**
	 * Registers the given listener for all thrown ConstraintViolationExceptions on objects of the given class
	 *
	 * @param listener the listener to register
	 * @param guardedClass guarded class or interface
	 * @return <code>true</code> if the listener was not yet registered
	 * @throws IllegalArgumentException if <code>listener == null</code> or <code>guardedClass == null</code>
	 */
	public boolean addListener(final ConstraintsViolatedListener listener, final Class< ? > guardedClass) throws IllegalArgumentException
	{
		Assert.argumentNotNull("listener", listener);
		Assert.argumentNotNull("guardedClass", guardedClass);

		isListenersFeatureUsed = true;

		synchronized (listenersByClass)
		{
			Set<ConstraintsViolatedListener> classListeners = listenersByClass.get(guardedClass);

			if (classListeners == null)
			{
				classListeners = new LinkedHashSet<>();
				listenersByClass.put(guardedClass, classListeners);
			}
			return classListeners.add(listener);
		}
	}

	/**
	 * Registers the given listener for all thrown ConstraintViolationExceptions on objects of the given object
	 *
	 * @param listener the listener to register
	 * @param guardedObject
	 * @return <code>true</code> if the listener was not yet registered
	 * @throws IllegalArgumentException if <code>listener == null</code> or <code>guardedObject == null</code>
	 */
	public boolean addListener(final ConstraintsViolatedListener listener, final Object guardedObject)
	{
		Assert.argumentNotNull("listener", listener);
		Assert.argumentNotNull("guardedObject", guardedObject);

		isListenersFeatureUsed = true;

		synchronized (listenersByObject)
		{
			Set<ConstraintsViolatedListener> objectListeners = listenersByObject.get(guardedObject);

			if (objectListeners == null)
			{
				objectListeners = new LinkedHashSet<>(2);
				listenersByObject.put(guardedObject, objectListeners);
			}
			return objectListeners.add(listener);
		}
	}

	/**
	 * @return the parameterNameResolver
	 */
	public ParameterNameResolver getParameterNameResolver()
	{
		return parameterNameResolver;
	}

	/**
	 * This method is provided for use by guard aspects.
	 *
	 * @throws ConstraintsViolatedException
	 * @throws ValidationFailedException
	 */
	protected void guardConstructorPost(final Object guardedObject, final Constructor< ? > ctor,
			@SuppressWarnings("unused") final Object[] args) throws ConstraintsViolatedException, ValidationFailedException
	{
		if (!isActivated) return;

		final ClassChecks cc = getClassChecks(ctor.getDeclaringClass());

		// check invariants
		if (isInvariantsEnabled && cc.isCheckInvariants || cc.methodsWithCheckInvariantsPost.contains(ctor))
		{
			final List<ConstraintViolation> violations = new ArrayList<>();
			try
			{
				validateInvariants(guardedObject, violations, null);
			}
			catch (final ValidationFailedException ex)
			{
				throw translateException(ex);
			}

			if (violations.size() > 0)
			{
				final ConstraintsViolatedException violationException = new ConstraintsViolatedException(violations);
				if (isListenersFeatureUsed) notifyListeners(guardedObject, violationException);

				throw translateException(violationException);
			}
		}
	}

	/**
	 * This method is provided for use by guard aspects.
	 *
	 * @throws ConstraintsViolatedException if anything precondition is not satisfied
	 * @throws ValidationFailedException
	 */
	protected void guardConstructorPre(final Object guardedObject, final Constructor< ? > ctor, final Object[] args)
			throws ConstraintsViolatedException, ValidationFailedException
	{
		if (!isActivated) return;

		// constructor parameter validation
		if (args.length > 0)
		{
			final List<ConstraintViolation> violations;
			try
			{
				violations = validateConstructorParameters(guardedObject, ctor, args);
			}
			catch (final ValidationFailedException ex)
			{
				throw translateException(ex);
			}

			if (violations != null)
			{
				final ConstraintsViolatedException violationException = new ConstraintsViolatedException(violations);
				if (isListenersFeatureUsed) notifyListeners(guardedObject, violationException);

				throw translateException(violationException);
			}
		}
	}

	/**
	 * This method is provided for use by guard aspects.
	 *
	 * @param guardedObject
	 * @param method
	 * @param args
	 * @param invocable
	 * @return The method return value.
	 * @throws ConstraintsViolatedException if an constraint violation occurs.
	 * @throws ValidationFailedException
	 */
	protected Object guardMethod(Object guardedObject, final Method method, final Object[] args, final Invocable invocable)
			throws Throwable
	{
		if (!isActivated) return invocable.invoke();

		final ClassChecks cc = getClassChecks(method.getDeclaringClass());

		final boolean checkInvariants = isInvariantsEnabled && cc.isCheckInvariants && !ReflectionUtils.isPrivate(method)
				&& !ReflectionUtils.isProtected(method);

		// if static method use the declaring class as guardedObject
		if (guardedObject == null && ReflectionUtils.isStatic(method)) guardedObject = method.getDeclaringClass();

		final List<ConstraintViolation> violations = new ArrayList<>();

		try
		{
			// check invariants
			if (checkInvariants || cc.methodsWithCheckInvariantsPre.contains(method)) validateInvariants(guardedObject, violations, null);

			// method parameter validation
			if (violations.size() == 0 && args.length > 0) validateMethodParameters(guardedObject, method, args, violations);
		}
		catch (final ValidationFailedException ex)
		{
			throw translateException(ex);
		}

		if (violations.size() > 0)
		{
			final ConstraintsViolatedException violationException = new ConstraintsViolatedException(violations);
			if (isListenersFeatureUsed) notifyListeners(guardedObject, violationException);

			throw translateException(violationException);
		}

		final Object returnValue = invocable.invoke();

		try
		{
			// check invariants if executed method is not private
			if (checkInvariants || cc.methodsWithCheckInvariantsPost.contains(method)) validateInvariants(guardedObject, violations, null);

			// method return value
			if (violations.size() == 0) validateMethodReturnValue(guardedObject, method, returnValue, violations);
		}
		catch (final ValidationFailedException ex)
		{
			throw translateException(ex);
		}

		if (violations.size() > 0)
		{
			final ConstraintsViolatedException violationException = new ConstraintsViolatedException(violations);
			if (isListenersFeatureUsed) notifyListeners(guardedObject, violationException);

			throw translateException(violationException);
		}

		return returnValue;
	}

	/**
	 * @param listener
	 * @return <code>true</code> if the listener is registered
	 * @throws IllegalArgumentException if <code>listener == null</code>
	 */
	public boolean hasListener(final ConstraintsViolatedListener listener) throws IllegalArgumentException
	{
		Assert.argumentNotNull("listener", listener);

		return listeners.contains(listener);
	}

	/**
	 * @param listener
	 * @param guardedClass guarded class or interface
	 * @return <code>true</code> if the listener is registered
	 * @throws IllegalArgumentException if <code>listener == null</code> or <code>guardedClass == null</code>
	 */
	public boolean hasListener(final ConstraintsViolatedListener listener, final Class< ? > guardedClass) throws IllegalArgumentException
	{
		Assert.argumentNotNull("listener", listener);
		Assert.argumentNotNull("guardedClass", guardedClass);

		final Set<ConstraintsViolatedListener> classListeners = listenersByClass.get(guardedClass);

		if (classListeners == null) return false;

		return classListeners.contains(listener);
	}

	/**
	 * @param listener
	 * @param guardedObject
	 * @return <code>true</code> if the listener is registered
	 * @throws IllegalArgumentException if <code>listener == null</code> or <code>guardedObject == null</code>
	 */
	public boolean hasListener(final ConstraintsViolatedListener listener, final Object guardedObject) throws IllegalArgumentException
	{
		Assert.argumentNotNull("listener", listener);
		Assert.argumentNotNull("guardedObject", guardedObject);

		final Set<ConstraintsViolatedListener> objectListeners = listenersByObject.get(guardedObject);

		if (objectListeners == null) return false;

		return objectListeners.contains(listener);
	}

	/**
	 * @return the isEnabled
	 */
	public boolean isActivated()
	{
		return isActivated;
	}


	/**
	 * Determines if invariants are checked prior and after every call to a non-private method or constructor.
	 *
	 * @return the isInvariantChecksActivated
	 */
	public boolean isInvariantsEnabled()
	{
		return isInvariantsEnabled;
	}

	/**
	 * Determines if invariants are checked prior and after every call to a non-private method or constructor.
	 *
	 * @param guardedClass the guarded class
	 * @return the isInvariantChecksActivated
	 */
	public boolean isInvariantsEnabled(final Class< ? > guardedClass)
	{
		return getClassChecks(guardedClass).isCheckInvariants;
	}

	/**
	 * notifies all registered validation listener about the occurred constraint violation exception
	 */
	protected void notifyListeners(final Object guardedObject, final ConstraintsViolatedException ex)
	{
		// happens for static methods
		if (guardedObject == null) return;

		final LinkedHashSet<ConstraintsViolatedListener> listenersToNotify = new LinkedHashSet<ConstraintsViolatedListener>();

		// get the object listeners
		{
			final Set<ConstraintsViolatedListener> objectListeners = listenersByObject.get(guardedObject);
			if (objectListeners != null) listenersToNotify.addAll(objectListeners);
		}

		// get the class listeners
		{
			final Set<ConstraintsViolatedListener> classListeners = listenersByClass.get(guardedObject.getClass());
			if (classListeners != null) listenersToNotify.addAll(classListeners);
		}

		// get the interface listeners
		{
			for (final Class< ? > interfaze : guardedObject.getClass().getInterfaces())
			{
				final Set<ConstraintsViolatedListener> interfaceListeners = listenersByClass.get(interfaze);
				if (interfaceListeners != null) listenersToNotify.addAll(interfaceListeners);
			}
		}

		// get the global listeners
		listenersToNotify.addAll(listeners);

		// notify the listeners
		for (final ConstraintsViolatedListener listener : listenersToNotify)
			try
			{
				listener.onConstraintsViolatedException(ex);
			}
			catch (final RuntimeException rex)
			{
				LOG.warn("Notifying listener '{}' failed.", listener, rex);
			}

	}

	/**
	 * Removes constraint checks from the given constructor parameter
	 *
	 * @param ctor
	 * @param paramIndex
	 * @param checks
	 * @throws InvalidConfigurationException if the declaring class is not guarded or the parameterIndex is out of range
	 */
	public void removeChecks(final Constructor< ? > ctor, final int paramIndex, final Check... checks) throws InvalidConfigurationException
	{
		Assert.argumentNotNull("ctor", ctor);
		Assert.argumentNotEmpty("checks", checks);

		getClassChecks(ctor.getDeclaringClass()).removeConstructorParameterChecks(ctor, paramIndex, checks);
	}

	/**
	 * Removes constraint checks for the given method parameter
	 *
	 * @param method
	 * @param paramIndex
	 * @param checks
	 * @throws IllegalArgumentException if <code>constructor == null</code> or <code>checks == null</code> or checks is
	 *             empty
	 * @throws InvalidConfigurationException if the parameterIndex is out of range
	 */
	public void removeChecks(final Method method, final int paramIndex, final Check... checks) throws InvalidConfigurationException
	{
		Assert.argumentNotNull("method", method);
		Assert.argumentNotEmpty("checks", checks);

		getClassChecks(method.getDeclaringClass()).removeMethodParameterChecks(method, paramIndex, checks);
	}

	/**
	 * Removes the given listener
	 *
	 * @param listener
	 * @return <code>true</code> if the listener was registered
	 * @throws IllegalArgumentException if <code>listener == null</code>
	 */
	public boolean removeListener(final ConstraintsViolatedListener listener) throws IllegalArgumentException
	{
		Assert.argumentNotNull("listener", listener);

		return listeners.remove(listener);
	}

	/**
	 * Removes the given listener
	 *
	 * @param listener
	 * @param guardedClass guarded class or interface
	 * @return <code>true</code> if the listener was registered
	 * @throws IllegalArgumentException if <code>listener == null</code> or <code>guardedClass == null</code>
	 */
	public boolean removeListener(final ConstraintsViolatedListener listener, final Class< ? > guardedClass)
			throws IllegalArgumentException
	{
		Assert.argumentNotNull("listener", listener);
		Assert.argumentNotNull("guardedClass", guardedClass);

		final Set<ConstraintsViolatedListener> currentListeners = listenersByClass.get(guardedClass);

		return currentListeners == null ? false : currentListeners.remove(listener);
	}

	/**
	 * Removes the given listener
	 *
	 * @param listener
	 * @param guardedObject
	 * @return <code>true</code> if the listener was registered
	 * @throws IllegalArgumentException if <code>listener == null</code> or <code>guardedObject == null</code>
	 */
	public boolean removeListener(final ConstraintsViolatedListener listener, final Object guardedObject) throws IllegalArgumentException
	{
		Assert.argumentNotNull("listener", listener);
		Assert.argumentNotNull("guardedObject", guardedObject);

		final Set<ConstraintsViolatedListener> currentListeners = listenersByObject.get(guardedObject);

		return currentListeners == null ? false : currentListeners.remove(listener);
	}

	/**
	 * If set to false OVal's programming by contract features are disabled and constraints are not checked
	 * automatically during runtime.
	 *
	 * @param isActivated the isActivated to set
	 */
	public void setActivated(final boolean isActivated)
	{
		this.isActivated = isActivated;
	}

	/**
	 * Specifies if invariants are checked prior and after calls to non-private methods and constructors.
	 *
	 * @param isEnabled the isInvariantsEnabled to set
	 */
	public void setInvariantsEnabled(final boolean isEnabled)
	{
		isInvariantsEnabled = isEnabled;
	}

	/**
	 * Specifies if invariants are checked prior and after calls to non-private methods and constructors.
	 *
	 * @param guardedClass the guarded class to turn on/off the invariant checking
	 * @param isEnabled the isEnabled to set
	 */
	public void setInvariantsEnabled(final Class< ? > guardedClass, final boolean isEnabled)
	{
		getClassChecks(guardedClass).isCheckInvariants = isEnabled;
	}

	/**
	 * @param parameterNameResolver the parameterNameResolver to set, cannot be null
	 * @throws IllegalArgumentException if <code>parameterNameResolver == null</code>
	 */
	public void setParameterNameResolver(final ParameterNameResolver parameterNameResolver) throws IllegalArgumentException
	{
		Assert.argumentNotNull("parameterNameResolver", parameterNameResolver);

		this.parameterNameResolver.setDelegate(parameterNameResolver);
	}

	/**
	 * Validates the give arguments against the defined constructor parameter constraints.<br>
	 *
	 * @return null if no violation, otherwise a list
	 * @throws ValidationFailedException
	 */
	protected List<ConstraintViolation> validateConstructorParameters(final Object validatedObject, final Constructor< ? > constructor,
			final Object[] argsToValidate) throws ValidationFailedException
	{
		// create required objects for this validation cycle
		final List<ConstraintViolation> violations = new ArrayList<>();

		try
		{
			final ClassChecks cc = getClassChecks(constructor.getDeclaringClass());
			final Map<Integer, ParameterChecks> parameterChecks = cc.checksForConstructorParameters.get(constructor);

			// if no parameter checks exist just return null
			if (parameterChecks == null) return null;

			final String[] parameterNames = parameterNameResolver.getParameterNames(constructor);

			for (int i = 0; i < argsToValidate.length; i++)
			{
				final ParameterChecks checks = parameterChecks.get(i);

				if (checks != null && checks.hasChecks())
				{
					final Object valueToValidate = argsToValidate[i];
					final ConstructorParameterContext context = new ConstructorParameterContext(constructor, i, parameterNames[i]);

					_validateParameterChecks(checks, validatedObject, valueToValidate, context, violations);
				}
			}
			return violations.size() == 0 ? null : violations;
		}
		catch (final OValException ex)
		{
			throw new ValidationFailedException("Validation of constructor parameters failed. Constructor: " + constructor
					+ " Validated object: " + validatedObject.getClass().getName() + "@" + Integer.toHexString(validatedObject.hashCode()),
					ex);
		}
	}

//	/**
//	 * {@inheritDoc}
//	 */
//	@Override
//	protected void validateInvariants(final Object guardedObject, final List<ConstraintViolation> violations, final String[] profiles)
//			throws IllegalArgumentException, ValidationFailedException
//	{
//			super.validateInvariants(guardedObject, violations, profiles);
//	}

	/**
	 * Validates the pre conditions for a method call.<br>
	 *
	 * @throws ValidationFailedException
	 */
	protected void validateMethodParameters(final Object validatedObject, final Method method, final Object[] args,
			final List<ConstraintViolation> violations) throws ValidationFailedException
	{
		// create a new set for this validation cycle
		try
		{
			final Map<Integer, ParameterChecks> parameterChecks = getClassChecks(method.getDeclaringClass()).checksForMethodParameters.get(method);

			if (parameterChecks == null) return;

			final String[] parameterNames = parameterNameResolver.getParameterNames(method);

			/*
			 * parameter constraints validation
			 */
			if (parameterNames.length > 0) for (int i = 0; i < args.length; i++)
			{
				final ParameterChecks checks = parameterChecks.get(i);

				if (checks != null && checks.hasChecks())
				{
					final Object valueToValidate = args[i];
					final MethodParameterContext context = new MethodParameterContext(method, i, parameterNames[i]);

					_validateParameterChecks(checks, validatedObject, valueToValidate, context, violations);
				}
			}
		}
		catch (final OValException ex)
		{
			throw new ValidationFailedException("Method pre conditions validation failed. Method: " + method + " Validated object: "
					+ validatedObject, ex);
		}
	}

	/**
	 * Validates the return value checks for a method call.<br>
	 *
	 * @throws ValidationFailedException
	 */
	protected void validateMethodReturnValue(final Object validatedObject, final Method method, final Object returnValue,
			final List<ConstraintViolation> violations) throws ValidationFailedException
	{
		final String key = System.identityHashCode(validatedObject) + " " + System.identityHashCode(method);

		/*
		 *  avoid circular references, e.g.
		 *
		 *  private String name;
		 *
		 *  @Assert("_this.name != null", lang="groovy")
		 *  public String getName { return name; }
		 *
		 *  => Groovy will invoke the getter to return the value, invocations of the getter will trigger the validation of the method return values again, including the @Assert constraint
		 */
		if (currentlyCheckingMethodReturnValues.contains(key)) return;

		currentlyCheckingMethodReturnValues.add(key);

		try
		{
			final ClassChecks cc = getClassChecks(method.getDeclaringClass());
			final Collection<Check> returnValueChecks = cc.checksForMethodReturnValues.get(method);

			if (returnValueChecks == null || returnValueChecks.size() == 0) return;

			final MethodReturnValueContext context = ContextCache.getMethodReturnValueContext(method);

			for (final Check check : returnValueChecks)
				checkConstraint(violations, check, validatedObject, returnValue, context, null, false);
		}
		catch (final OValException ex)
		{
			throw new ValidationFailedException("Method post conditions validation failed. Method: " + method + " Validated object: "
					+ validatedObject, ex);
		}
		finally
		{
			currentlyCheckingMethodReturnValues.remove(key);
		}
	}
}
