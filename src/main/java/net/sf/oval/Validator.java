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
package net.sf.oval;

import net.sf.oval.configuration.Configurer;
import net.sf.oval.configuration.annotation.AnnotationsConfigurer;
import net.sf.oval.configuration.annotation.JPAAnnotationsConfigurer;
import net.sf.oval.configuration.pojo.POJOConfigurer;
import net.sf.oval.configuration.pojo.elements.ClassConfiguration;
import net.sf.oval.configuration.pojo.elements.ConstraintSetConfiguration;
import net.sf.oval.configuration.pojo.elements.ConstructorConfiguration;
import net.sf.oval.configuration.pojo.elements.FieldConfiguration;
import net.sf.oval.configuration.pojo.elements.MethodConfiguration;
import net.sf.oval.configuration.pojo.elements.ObjectConfiguration;
import net.sf.oval.configuration.pojo.elements.ParameterConfiguration;
import net.sf.oval.configuration.xml.XMLConfigurer;
import net.sf.oval.constraint.AssertConstraintSetCheck;
import net.sf.oval.constraint.AssertFieldConstraintsCheck;
import net.sf.oval.constraint.AssertValidCheck;
import net.sf.oval.constraint.NotNullCheck;
import net.sf.oval.context.ConstructorParameterContext;
import net.sf.oval.context.FieldContext;
import net.sf.oval.context.MethodParameterContext;
import net.sf.oval.context.MethodReturnValueContext;
import net.sf.oval.context.OValContext;
import net.sf.oval.exception.ConstraintSetAlreadyDefinedException;
import net.sf.oval.exception.ConstraintsViolatedException;
import net.sf.oval.exception.ExceptionTranslator;
import net.sf.oval.exception.FieldNotFoundException;
import net.sf.oval.exception.InvalidConfigurationException;
import net.sf.oval.exception.MethodNotFoundException;
import net.sf.oval.exception.OValException;
import net.sf.oval.exception.ReflectionException;
import net.sf.oval.exception.UndefinedConstraintSetException;
import net.sf.oval.exception.ValidationFailedException;
import net.sf.oval.expression.ExpressionLanguageRegistry;
import net.sf.oval.guard.ParameterNameResolver;
import net.sf.oval.guard.ParameterNameResolverEnumerationImpl;
import net.sf.oval.internal.ClassChecks;
import net.sf.oval.internal.ContextCache;
import net.sf.oval.internal.MessageRenderer;
import net.sf.oval.internal.util.ArrayUtils;
import net.sf.oval.internal.util.Assert;
import net.sf.oval.internal.util.IdentitySet;
import net.sf.oval.internal.util.ReflectionUtils;
import net.sf.oval.internal.util.StringUtils;
import net.sf.oval.internal.util.ThreadLocalLinkedList;
import net.sf.oval.localization.context.OValContextRenderer;
import net.sf.oval.localization.context.ToStringValidationContextRenderer;
import net.sf.oval.localization.locale.LocaleProvider;
import net.sf.oval.localization.locale.ThreadLocalLocaleProvider;
import net.sf.oval.localization.message.MessageResolver;
import net.sf.oval.localization.message.ResourceBundleMessageResolver;
import net.sf.oval.localization.value.MessageValueFormatter;
import net.sf.oval.localization.value.ToStringMessageValueFormatter;
import net.sf.oval.ogn.ObjectGraphNavigationResult;
import net.sf.oval.ogn.ObjectGraphNavigatorRegistry;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import static java.lang.Boolean.TRUE;

/**
 * <p>Instances of this class can validate objects based on declared constraints.
 * Constraints can either be declared using OVal's constraint annotations, XML configuration
 * files or EJB3 JPA annotations.</p>
 *
 * <p>This class is thread-safe.</p>
 *
 * @author Sebastian Thomschke
 *
 * @see AnnotationsConfigurer
 * @see JPAAnnotationsConfigurer
 * @see POJOConfigurer
 * @see XMLConfigurer
 */
public class Validator implements IValidator
{
	protected static final class DelegatingParameterNameResolver implements ParameterNameResolver
	{
		private ParameterNameResolver delegate;

		public DelegatingParameterNameResolver(final ParameterNameResolver delegate)
		{
			this.delegate = delegate;
		}

		public ParameterNameResolver getDelegate()
		{
			return delegate;
		}

		/**
		 * {@inheritDoc}
		 */
		public String[] getParameterNames(final Constructor< ? > constructor) throws ReflectionException
		{
			return delegate.getParameterNames(constructor);
		}

		/**
		 * {@inheritDoc}
		 */
		public String[] getParameterNames(final Method method) throws ReflectionException
		{
			return delegate.getParameterNames(method);
		}

		public void setDelegate(final ParameterNameResolver delegate)
		{
			this.delegate = delegate;
		}
	}

	public static OValContextRenderer getContextRenderer()
	{
		return contextRenderer;
	}

	public static LocaleProvider getLocaleProvider()
	{
		return localeProvider;
	}

//MASE public static LoggerFactory getLoggerFactory()
//	{
//		return Log.getLoggerFactory();
//	}

	public static MessageResolver getMessageResolver()
	{
		/*
		 * since ResourceBundleMessageResolver references getCollectionFactory() of this class
		 * we are lazy referencing the resolvers shared instance.
		 */
		if (messageResolver == null)
		{
			messageResolver = ResourceBundleMessageResolver.INSTANCE;
		}
		return messageResolver;
	}

	public static MessageValueFormatter getMessageValueFormatter()
	{
		return messageValueFormatter;
	}

	/**
	 * @param contextRenderer the contextRenderer to set
	 */
	public static void setContextRenderer(final OValContextRenderer contextRenderer)
	{
		Assert.argumentNotNull("contextRenderer", contextRenderer);
		Validator.contextRenderer = contextRenderer;
	}

	public static void setLocaleProvider(final LocaleProvider localeProvider)
	{
		Assert.argumentNotNull("localeProvider", localeProvider);
		Validator.localeProvider = localeProvider;
	}

//	/**
//	 * @param loggerFactory the loggerFactory to set
//	 */
//	public static void setLoggerFactory(final LoggerFactory loggerFactory)
//	{
//		Assert.argumentNotNull("loggerFactory", loggerFactory);
//		Log.setLoggerFactory(loggerFactory);
//	}

	/**
	 * @param messageResolver the messageResolver to set
	 * @throws IllegalArgumentException if <code>messageResolver == null</code>
	 */
	public static void setMessageResolver(final MessageResolver messageResolver) throws IllegalArgumentException
	{
		Assert.argumentNotNull("messageResolver", messageResolver);
		Validator.messageResolver = messageResolver;
	}

	/**
	 * @param formatter the messageValueFormatter to set
	 */
	public static void setMessageValueFormatter(final MessageValueFormatter formatter)
	{
		Assert.argumentNotNull("formatter", formatter);
		Validator.messageValueFormatter = formatter;
	}

//	private static final Log LOG = Log.getLog(Validator.class);

	//private static CollectionFactory collectionFactory = _createDefaultCollectionFactory();

	private static OValContextRenderer contextRenderer = ToStringValidationContextRenderer.INSTANCE;

	private static MessageResolver messageResolver;

	private static MessageValueFormatter messageValueFormatter = ToStringMessageValueFormatter.INSTANCE;

	private static LocaleProvider localeProvider = new ThreadLocalLocaleProvider();

	private final Map<Class< ? >, ClassChecks> checksByClass = new WeakHashMap<Class< ? >, ClassChecks>();

	private final Set<Configurer> configurers = new LinkedHashSet<>(4);

	private final Map<String, ConstraintSet> constraintSetsById = new LinkedHashMap<>(4);

	protected final ThreadLocalLinkedList<Set<Object>> currentlyValidatedObjects = new ThreadLocalLinkedList<Set<Object>>();

	protected final ThreadLocalLinkedList<List<ConstraintViolation>> currentViolations = new ThreadLocalLinkedList<List<ConstraintViolation>>();

	private final Set<String> disabledProfiles = new LinkedHashSet<>();

	private final Set<String> enabledProfiles = new LinkedHashSet<>();

	private ExceptionTranslator exceptionTranslator;

	protected final ExpressionLanguageRegistry expressionLanguageRegistry = new ExpressionLanguageRegistry();

	private boolean isAllProfilesEnabledByDefault = true;

	/**
	 * Flag that indicates any configuration method related to profiles was called.
	 * Used for performance improvements.
	 */
	private boolean isProfilesFeatureUsed = false;

	private final ObjectGraphNavigatorRegistry ognRegistry = new ObjectGraphNavigatorRegistry();

	protected final DelegatingParameterNameResolver parameterNameResolver = new DelegatingParameterNameResolver(
			new ParameterNameResolverEnumerationImpl());

	/**
	 * Constructs a new validator instance and uses a new instance of AnnotationsConfigurer
	 */
	public Validator()
	{
		ReflectionUtils.assertPrivateAccessAllowed();
		configurers.add(new AnnotationsConfigurer());
	}

	/**
	 * Constructs a new validator instance and configures it using the given configurers
	 *
	 * @param configurers
	 */
	public Validator(final Collection<Configurer> configurers)
	{
		ReflectionUtils.assertPrivateAccessAllowed();
		if (configurers != null)
		{
			this.configurers.addAll(configurers);
		}
	}

	/**
	 * Constructs a new validator instance and configures it using the given configurers
	 *
	 * @param configurers
	 */
	public Validator(final Configurer... configurers)
	{
		ReflectionUtils.assertPrivateAccessAllowed();
		if (configurers != null)
		{
            Collections.addAll(this.configurers, configurers);
		}
	}

	private void _addChecks(final ClassChecks cc, final ClassConfiguration classCfg) throws InvalidConfigurationException,
	ReflectionException
	{
		if (classCfg.overwrite)
		{
			cc.clear();
		}

//		if (classCfg.checkInvariants != null)
//		{
			cc.isCheckInvariants = classCfg.checkInvariants;
//		}

		// cache the result for better performance
		final boolean applyFieldConstraintsToConstructors = classCfg.applyFieldConstraintsToConstructors;
		final boolean applyFieldConstraintsToSetters = classCfg.applyFieldConstraintsToSetters;
		final boolean assertParametersNotNull = classCfg.assertParametersNotNull;
		final NotNullCheck sharedNotNullCheck = assertParametersNotNull ? new NotNullCheck() : null;

		try
		{
			/* ******************************
			 * apply object level checks
			 * ******************************/
			if (classCfg.objectConfiguration != null)
			{
				final ObjectConfiguration objectCfg = classCfg.objectConfiguration;

				if (objectCfg.overwrite)
				{
					cc.clearObjectChecks();
				}
				cc.addObjectChecks(objectCfg.checks);
			}

			/* ******************************
			 * apply field checks
			 * ******************************/
			if (classCfg.fieldConfigurations != null)
			{
				for (final FieldConfiguration fieldCfg : classCfg.fieldConfigurations)
				{
					final Field field = classCfg.type.getDeclaredField(fieldCfg.name);

					if (fieldCfg.overwrite)
					{
						cc.clearFieldChecks(field);
					}

					if (fieldCfg.checks != null && fieldCfg.checks.size() > 0)
					{
						cc.addFieldChecks(field, fieldCfg.checks);
					}
				}
			}

			/* ******************************
			 * apply constructor parameter checks
			 * ******************************/
			if (classCfg.constructorConfigurations != null)
			{
				for (final ConstructorConfiguration ctorCfg : classCfg.constructorConfigurations)
				{
					// ignore constructors without parameters
					if (ctorCfg.parameterConfigurations == null)
					{
						continue;
					}

					final Class< ? >[] paramTypes = new Class[ctorCfg.parameterConfigurations.size()];

					for (int i = 0, l = ctorCfg.parameterConfigurations.size(); i < l; i++)
					{
						paramTypes[i] = ctorCfg.parameterConfigurations.get(i).type;
					}

					final Constructor< ? > ctor = classCfg.type.getDeclaredConstructor(paramTypes);

					if (ctorCfg.overwrite)
					{
						cc.clearConstructorChecks(ctor);
					}

					if (ctorCfg.postCheckInvariants)
					{
						cc.methodsWithCheckInvariantsPost.add(ctor);
					}

					final String[] paramNames = parameterNameResolver.getParameterNames(ctor);

					for (int i = 0, l = ctorCfg.parameterConfigurations.size(); i < l; i++)
					{
						final ParameterConfiguration paramCfg = ctorCfg.parameterConfigurations.get(i);

						if (paramCfg.overwrite)
						{
							cc.clearConstructorParameterChecks(ctor, i);
						}

						if (paramCfg.hasChecks())
						{
							cc.addConstructorParameterChecks(ctor, i, paramCfg.checks);
						}

						if (paramCfg.hasCheckExclusions())
						{
							cc.addConstructorParameterCheckExclusions(ctor, i, paramCfg.checkExclusions);
						}

						if (assertParametersNotNull)
						{
							cc.addConstructorParameterChecks(ctor, i, sharedNotNullCheck);
						}

						/* *******************
						 * applying field constraints to the single parameter of setter methods
						 * *******************/
						if (applyFieldConstraintsToConstructors)
						{
							final Field field = ReflectionUtils.getField(cc.clazz, paramNames[i]);

							// check if a corresponding field has been found
							if (field != null && paramTypes[i].isAssignableFrom(field.getType()))
							{
								final AssertFieldConstraintsCheck check = new AssertFieldConstraintsCheck();
								check.setFieldName(field.getName());
								cc.addConstructorParameterChecks(ctor, i, check);
							}
						}
					}
				}
			}

			/* ******************************
			 * apply method parameter and return value checks and pre/post conditions
			 * ******************************/
			if (classCfg.methodConfigurations != null)
			{
				for (final MethodConfiguration methodCfg : classCfg.methodConfigurations)
				{
					/* ******************************
					 * determine the method
					 * ******************************/
					final Method method;

					if (methodCfg.parameterConfigurations == null || methodCfg.parameterConfigurations.size() == 0)
					{
						method = classCfg.type.getDeclaredMethod(methodCfg.name);
					}
					else
					{
						final Class< ? >[] paramTypes = new Class[methodCfg.parameterConfigurations.size()];

						for (int i = 0, l = methodCfg.parameterConfigurations.size(); i < l; i++)
						{
							paramTypes[i] = methodCfg.parameterConfigurations.get(i).type;
						}

						method = classCfg.type.getDeclaredMethod(methodCfg.name, paramTypes);
					}

					if (methodCfg.overwrite)
					{
						cc.clearMethodChecks(method);
					}

					/* ******************************
					 * applying field constraints to the single parameter of setter methods
					 * ******************************/
					if (applyFieldConstraintsToSetters)
					{
						final Field field = ReflectionUtils.getFieldForSetter(method);

						// check if a corresponding field has been found
						if (field != null)
						{
							final AssertFieldConstraintsCheck check = new AssertFieldConstraintsCheck();
							check.setFieldName(field.getName());
							cc.addMethodParameterChecks(method, 0, check);
						}
					}

					/* ******************************
					 * configure parameter constraints
					 * ******************************/
					if (methodCfg.parameterConfigurations != null && methodCfg.parameterConfigurations.size() > 0)
					{
						for (int i = 0, l = methodCfg.parameterConfigurations.size(); i < l; i++)
						{
							final ParameterConfiguration paramCfg = methodCfg.parameterConfigurations.get(i);

							if (paramCfg.overwrite)
							{
								cc.clearMethodParameterChecks(method, i);
							}

							if (paramCfg.hasChecks())
							{
								cc.addMethodParameterChecks(method, i, paramCfg.checks);
							}

							if (paramCfg.hasCheckExclusions())
							{
								cc.addMethodParameterCheckExclusions(method, i, paramCfg.checkExclusions);
							}

							if (assertParametersNotNull)
							{
								cc.addMethodParameterChecks(method, i, sharedNotNullCheck);
							}
						}
					}

					/* ******************************
					 * configure return value constraints
					 * ******************************/
					if (methodCfg.returnValueConfiguration != null)
					{
						if (methodCfg.returnValueConfiguration.overwrite)
						{
							cc.clearMethodReturnValueChecks(method);
						}

						if (methodCfg.returnValueConfiguration.checks != null && methodCfg.returnValueConfiguration.checks.size() > 0)
						{
							cc.addMethodReturnValueChecks(method, methodCfg.isInvariant, methodCfg.returnValueConfiguration.checks);
						}
					}

					if (methodCfg.preCheckInvariants)
					{
						cc.methodsWithCheckInvariantsPre.add(method);
					}

					/*
					 * configure pre conditions
					 */
					if (methodCfg.preExecutionConfiguration != null)
					{
						if (methodCfg.preExecutionConfiguration.overwrite)
						{
							cc.clearMethodPreChecks(method);
						}

						if (methodCfg.preExecutionConfiguration.checks != null && methodCfg.preExecutionConfiguration.checks.size() > 0)
						{
							cc.addMethodPreChecks(method, methodCfg.preExecutionConfiguration.checks);
						}
					}

					if (methodCfg.postCheckInvariants)
					{
						cc.methodsWithCheckInvariantsPost.add(method);
					}

					/*
					 * configure post conditions
					 */
					if (methodCfg.postExecutionConfiguration != null)
					{
						if (methodCfg.postExecutionConfiguration.overwrite)
						{
							cc.clearMethodPostChecks(method);
						}

						if (methodCfg.postExecutionConfiguration.checks != null && methodCfg.postExecutionConfiguration.checks.size() > 0)
						{
							cc.addMethodPostChecks(method, methodCfg.postExecutionConfiguration.checks);
						}
					}
				}
			}
		}
		catch (final NoSuchMethodException ex)
		{
			throw new MethodNotFoundException(ex);
		}
		catch (final NoSuchFieldException ex)
		{
			throw new FieldNotFoundException(ex);
		}
	}

	private void _checkConstraint(final List<ConstraintViolation> violations, final Check check, final Object validatedObject,
			final Object valueToValidate, final OValContext context, final String[] profiles)
	{
		/*
		 * special handling of the AssertValid constraint
		 */
		if (check instanceof AssertValidCheck)
		{
			checkConstraintAssertValid(violations, (AssertValidCheck) check, validatedObject, valueToValidate, context, profiles);
			return;
		}

		/*
		 * special handling of the FieldConstraints constraint
		 */
		if (check instanceof AssertConstraintSetCheck)
		{
			checkConstraintAssertConstraintSet(violations, (AssertConstraintSetCheck) check, validatedObject, valueToValidate, context,
					profiles);
			return;
		}

		/*
		 * special handling of the FieldConstraints constraint
		 */
		if (check instanceof AssertFieldConstraintsCheck)
		{
			checkConstraintAssertFieldConstraints(violations, (AssertFieldConstraintsCheck) check, validatedObject, valueToValidate,
					context, profiles);
			return;
		}

		/*
		 * standard constraints handling
		 */
		if (!check.isSatisfied(validatedObject, valueToValidate, context, this))
		{
			final String errorMessage = renderMessage(context, valueToValidate, check.getMessage(), check.getMessageVariables());
			violations.add(new ConstraintViolation(check, errorMessage, validatedObject, valueToValidate, context));
		}
	}

	/**
	 * validate validatedObject based on the constraints of the given class
	 */
	private void _validateObjectInvariants(final Object validatedObject, final Class< ? > clazz,
			final List<ConstraintViolation> violations, final String[] profiles) throws ValidationFailedException
	{
		assert validatedObject != null;
		assert clazz != null;
		assert violations != null;

		// abort if the root class has been reached
		if (clazz == Object.class) return;

		try
		{
			final ClassChecks cc = getClassChecks(clazz);

			// validate field constraints
			for (final Field field : cc.constrainedFields)
			{
				final Collection<Check> checks = cc.checksForFields.get(field);

				if (checks != null && checks.size() > 0)
				{
					final FieldContext ctx = ContextCache.getFieldContext(field);
					final Object valueToValidate = resolveValue(ctx, validatedObject);

					for (final Check check : checks)
					{
						checkConstraint(violations, check, validatedObject, valueToValidate, ctx, profiles, false);
					}
				}
			}

			// validate constraints on getter methods
			for (final Method getter : cc.constrainedMethods)
			{
				final Collection<Check> checks = cc.checksForMethodReturnValues.get(getter);

				if (checks != null && checks.size() > 0)
				{
					final MethodReturnValueContext ctx = ContextCache.getMethodReturnValueContext(getter);
					final Object valueToValidate = resolveValue(ctx, validatedObject);

					for (final Check check : checks)
					{
						checkConstraint(violations, check, validatedObject, valueToValidate, ctx, profiles, false);
					}
				}
			}

			// validate object constraints
			if (cc.checksForObject.size() > 0)
			{
				final OValContext ctx = ContextCache.getClassContext(clazz);
				for (final Check check : cc.checksForObject)
				{
					checkConstraint(violations, check, validatedObject, validatedObject, ctx, profiles, false);
				}
			}

			// if the super class is annotated to be validatable also validate it against the object
			_validateObjectInvariants(validatedObject, clazz.getSuperclass(), violations, profiles);
		}
		catch (final OValException ex)
		{
			throw new ValidationFailedException("Object validation failed. Class: " + clazz + " Validated object: " + validatedObject, ex);
		}
	}

	/**
	 * Validates the static field and static getter constrains of the given class.
	 * Constraints specified for super classes are not taken in account.
	 */
	private void _validateStaticInvariants(final Class< ? > validatedClass, final List<ConstraintViolation> violations,
			final String[] profiles) throws ValidationFailedException
	{
		assert validatedClass != null;
		assert violations != null;

		final ClassChecks cc = getClassChecks(validatedClass);

		// validate static field constraints
		for (final Field field : cc.constrainedStaticFields)
		{
			final Collection<Check> checks = cc.checksForFields.get(field);

			if (checks != null && checks.size() > 0)
			{
				final FieldContext ctx = ContextCache.getFieldContext(field);
				final Object valueToValidate = resolveValue(ctx, null);

				for (final Check check : checks)
				{
					checkConstraint(violations, check, validatedClass, valueToValidate, ctx, profiles, false);
				}
			}
		}

		// validate constraints on getter methods
		for (final Method getter : cc.constrainedStaticMethods)
		{
			final Collection<Check> checks = cc.checksForMethodReturnValues.get(getter);

			if (checks != null && checks.size() > 0)
			{
				final MethodReturnValueContext ctx = ContextCache.getMethodReturnValueContext(getter);
				final Object valueToValidate = resolveValue(ctx, null);

				for (final Check check : checks)
				{
					checkConstraint(violations, check, validatedClass, valueToValidate, ctx, profiles, false);
				}
			}
		}
	}

	/**
	 * Registers object-level constraint checks
	 *
	 * @param clazz the class to register the checks for
	 * @param checks the checks to add
	 * @throws IllegalArgumentException if <code>clazz == null</code> or <code>checks == null</code> or checks is empty
	 */
	public void addChecks(final Class< ? > clazz, final Check... checks) throws IllegalArgumentException
	{
		Assert.argumentNotNull("clazz", clazz);
		Assert.argumentNotEmpty("checks", checks);

		getClassChecks(clazz).addObjectChecks(checks);
	}

	/**
	 * Registers constraint checks for the given field
	 *
	 * @param field the field to declare the checks for
	 * @param checks the checks to add
	 * @throws IllegalArgumentException if <code>field == null</code> or <code>checks == null</code> or checks is empty
	 */
	public void addChecks(final Field field, final Check... checks) throws IllegalArgumentException
	{
		Assert.argumentNotNull("field", field);
		Assert.argumentNotEmpty("checks", checks);

		getClassChecks(field.getDeclaringClass()).addFieldChecks(field, checks);
	}

	/**
	 * Registers constraint checks for the given getter's return value
	 *
	 * @param invariantMethod a non-void, non-parameterized method (usually a JavaBean Getter style method)
	 * @param checks the checks to add
	 * @throws IllegalArgumentException if <code>getter == null</code> or <code>checks == null</code>
	 * @throws InvalidConfigurationException if getter is not a getter method
	 */
	public void addChecks(final Method invariantMethod, final Check... checks) throws IllegalArgumentException,
	InvalidConfigurationException
	{
		Assert.argumentNotNull("invariantMethod", invariantMethod);
		Assert.argumentNotEmpty("checks", checks);

		getClassChecks(invariantMethod.getDeclaringClass()).addMethodReturnValueChecks(invariantMethod, TRUE, checks);
	}

	/**
	 * Registers a new constraint set.
	 *
	 * @param constraintSet cannot be null
	 * @param overwrite
	 * @throws ConstraintSetAlreadyDefinedException if <code>overwrite == false</code> and
	 * 												a constraint set with the given id exists already
	 * @throws IllegalArgumentException if <code>constraintSet == null</code>
	 * 									or <code>constraintSet.id == null</code>
	 * 									or <code>constraintSet.id.length == 0</code>
	 * @throws IllegalArgumentException if <code>constraintSet.id == null</code>
	 */
	public void addConstraintSet(final ConstraintSet constraintSet, final boolean overwrite) throws ConstraintSetAlreadyDefinedException,
	IllegalArgumentException
	{
		Assert.argumentNotNull("constraintSet", constraintSet);
		Assert.argumentNotEmpty("constraintSet.id", constraintSet.getId());

		synchronized (constraintSetsById)
		{
			if (!overwrite && constraintSetsById.containsKey(constraintSet.getId()))
				throw new ConstraintSetAlreadyDefinedException(constraintSet.getId());

			constraintSetsById.put(constraintSet.getId(), constraintSet);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void assertValid(final Object validatedObject) throws IllegalArgumentException, ValidationFailedException,
	ConstraintsViolatedException
	{
		final List<ConstraintViolation> violations = validate(validatedObject);

		if (violations.size() > 0) throw translateException(new ConstraintsViolatedException(violations));
	}

	/**
	 * {@inheritDoc}
	 */
	public void assertValidFieldValue(final Object validatedObject, final Field validatedField, final Object fieldValueToValidate)
			throws IllegalArgumentException, ValidationFailedException, ConstraintsViolatedException
	{
		final List<ConstraintViolation> violations = validateFieldValue(validatedObject, validatedField, fieldValueToValidate);

		if (violations.size() > 0) throw translateException(new ConstraintsViolatedException(violations));
	}

	protected void checkConstraint(final List<ConstraintViolation> violations, final Check check, Object validatedObject,
			Object valueToValidate, OValContext context, final String[] profiles, final boolean isContainerValue) throws OValException
	{
		if (!isAnyProfileEnabled(check.getProfiles(), profiles)) return;

		if (!check.isActive(validatedObject, valueToValidate, this)) return;

		final ConstraintTarget[] targets = check.getAppliesTo();

		// only process the target expression if we are not already on a value inside the container object (collection, array, map)
		if (!isContainerValue)
		{
			String target = check.getTarget();
			if (target != null)
			{
				target = target.trim();
				if (target.length() > 0)
				{
					if (valueToValidate == null) return;
					final String[] chunks = target.split(":", 2);
					final String ognId, path;
					if (chunks.length == 1)
					{
						ognId = "";
						path = chunks[0];
					}
					else
					{
						ognId = chunks[0];
						path = chunks[1];
					}
					final ObjectGraphNavigationResult result = ognRegistry.getObjectGraphNavigator(ognId) //
							.navigateTo(valueToValidate, path);
					if (result == null) return;
					validatedObject = result.targetParent;
					valueToValidate = result.target;
					if (result.targetAccessor instanceof Field)
					{
						context = ContextCache.getFieldContext((Field) result.targetAccessor);
					}
					else
					{
						context = ContextCache.getMethodReturnValueContext((Method) result.targetAccessor);
					}
				}
			}
		}

		final Class< ? > compileTimeType = context.getCompileTimeType();

		final boolean isCollection = valueToValidate != null ? //
				valueToValidate instanceof Collection< ? > : //
					Collection.class.isAssignableFrom(compileTimeType);
		final boolean isMap = !isCollection && //
				(valueToValidate != null ? //
						valueToValidate instanceof Map< ? , ? > : //
							Map.class.isAssignableFrom(compileTimeType));
		final boolean isArray = !isCollection && !isMap && //
				(valueToValidate != null ? //
						valueToValidate.getClass().isArray() : //
							compileTimeType.isArray());
		final boolean isContainer = isCollection || isMap || isArray;

		if (isContainer && valueToValidate != null) if (isCollection)
		{
			if (ArrayUtils.containsSame(targets, ConstraintTarget.VALUES))
			{
				for (final Object item : (Collection< ? >) valueToValidate)
				{
					checkConstraint(violations, check, validatedObject, item, context, profiles, true);
				}
			}
		}
		else if (isMap)
		{
			if (ArrayUtils.containsSame(targets, ConstraintTarget.KEYS))
			{
				for (final Object item : ((Map< ? , ? >) valueToValidate).keySet())
				{
					checkConstraint(violations, check, validatedObject, item, context, profiles, true);
				}
			}

			if (ArrayUtils.containsSame(targets, ConstraintTarget.VALUES))
			{
				for (final Object item : ((Map< ? , ? >) valueToValidate).values())
				{
					checkConstraint(violations, check, validatedObject, item, context, profiles, true);
				}
			}
		}
		else if (ArrayUtils.containsSame(targets, ConstraintTarget.VALUES))
		{
			for (final Object item : ArrayUtils.asList(valueToValidate))
			{
				checkConstraint(violations, check, validatedObject, item, context, profiles, true);
			}
		}
		if (isContainerValue || !isContainer || isContainer && ArrayUtils.containsSame(targets, ConstraintTarget.CONTAINER))
		{
			_checkConstraint(violations, check, validatedObject, valueToValidate, context, profiles);
		}
	}

	protected void checkConstraintAssertConstraintSet(final List<ConstraintViolation> violations, final AssertConstraintSetCheck check,
			final Object validatedObject, final Object valueToValidate, final OValContext context, final String[] profiles)
					throws OValException
	{
		final ConstraintSet cs = getConstraintSet(check.getId());

		if (cs == null) throw new UndefinedConstraintSetException(check.getId());

		final Collection<Check> referencedChecks = cs.getChecks();

		if (referencedChecks != null && referencedChecks.size() > 0)
		{
			for (final Check referencedCheck : referencedChecks)
			{
				checkConstraint(violations, referencedCheck, validatedObject, valueToValidate, context, profiles, false);
			}
		}
	}

	protected void checkConstraintAssertFieldConstraints(final List<ConstraintViolation> violations,
			final AssertFieldConstraintsCheck check, final Object validatedObject, final Object valueToValidate, final OValContext context,
			final String[] profiles) throws OValException
	{
		final Class< ? > targetClass;

		/*
		 * set the targetClass based on the validation context
		 */
		if (check.getDeclaringClass() != null && check.getDeclaringClass() != Void.class)
		{
			targetClass = check.getDeclaringClass();
		}
		else if (context instanceof ConstructorParameterContext)
		{
			// the class declaring the field must either be the class declaring the constructor or one of its super
			// classes
			targetClass = ((ConstructorParameterContext) context).getConstructor().getDeclaringClass();
		}
		else if (context instanceof MethodParameterContext)
		{
			// the class declaring the field must either be the class declaring the method or one of its super classes
			targetClass = ((MethodParameterContext) context).getMethod().getDeclaringClass();
		}
		else if (context instanceof MethodReturnValueContext)
		{
			// the class declaring the field must either be the class declaring the getter or one of its super classes
			targetClass = ((MethodReturnValueContext) context).getMethod().getDeclaringClass();
		}
		else
		{
			// the lowest class that is expected to declare the field (or one of its super classes)
			targetClass = validatedObject.getClass();
		}

		// the name of the field whose constraints shall be used
		String fieldName = check.getFieldName();

		/*
		 * calculate the field name based on the validation context if the @AssertFieldConstraints constraint didn't specify the field name
		 */
		if (fieldName == null || fieldName.length() == 0) if (context instanceof ConstructorParameterContext)
		{
			fieldName = ((ConstructorParameterContext) context).getParameterName();
		}
		else if (context instanceof MethodParameterContext)
		{
			fieldName = ((MethodParameterContext) context).getParameterName();
		}
		else if (context instanceof MethodReturnValueContext)
		{
			fieldName = ReflectionUtils.guessFieldName(((MethodReturnValueContext) context).getMethod());
		}

		/*
		 * find the field based on fieldName and targetClass
		 */
		final Field field = ReflectionUtils.getFieldRecursive(targetClass, fieldName);

		if (field == null)
			throw new FieldNotFoundException("Field <" + fieldName + "> not found in class <" + targetClass + "> or its super classes.");

		final ClassChecks cc = getClassChecks(field.getDeclaringClass());
		final Collection<Check> referencedChecks = cc.checksForFields.get(field);
		if (referencedChecks != null && referencedChecks.size() > 0)
		{
			for (final Check referencedCheck : referencedChecks)
			{
				checkConstraint(violations, referencedCheck, validatedObject, valueToValidate, context, profiles, false);
			}
		}
	}

	protected void checkConstraintAssertValid(final List<ConstraintViolation> violations, final AssertValidCheck check,
			final Object validatedObject, final Object valueToValidate, final OValContext context, final String[] profiles)
					throws OValException
	{
		if (valueToValidate == null) return;

		// ignore circular dependencies
		if (isCurrentlyValidated(valueToValidate)) return;

		final List<ConstraintViolation> additionalViolations = new ArrayList<>();
		validateInvariants(valueToValidate, additionalViolations, profiles);

		if (additionalViolations.size() != 0)
		{
			final String errorMessage = renderMessage(context, valueToValidate, check.getMessage(), check.getMessageVariables());

			violations.add(new ConstraintViolation(check, errorMessage, validatedObject, valueToValidate, context, additionalViolations));
		}
	}

	/**
	 * Disables all constraints profiles globally, i.e. no configured constraint will be validated.
	 */
	public synchronized void disableAllProfiles()
	{
		isProfilesFeatureUsed = true;
		isAllProfilesEnabledByDefault = false;

		enabledProfiles.clear();
		disabledProfiles.clear();
	}

	/**
	 * Disables a constraints profile globally.
	 * @param profile the id of the profile
	 */
	public void disableProfile(final String profile)
	{
		isProfilesFeatureUsed = true;

		if (isAllProfilesEnabledByDefault)
		{
			disabledProfiles.add(profile);
		}
		else
		{
			enabledProfiles.remove(profile);
		}
	}

	/**
	 * Enables all constraints profiles globally, i.e. all configured constraint will be validated.
	 */
	public synchronized void enableAllProfiles()
	{
		isProfilesFeatureUsed = true;
		isAllProfilesEnabledByDefault = true;

		enabledProfiles.clear();
		disabledProfiles.clear();
	}

	/**
	 * Enables a constraints profile globally.
	 * @param profile the id of the profile
	 */
	public void enableProfile(final String profile)
	{
		isProfilesFeatureUsed = true;

		if (isAllProfilesEnabledByDefault)
		{
			disabledProfiles.remove(profile);
		}
		else
		{
			enabledProfiles.add(profile);
		}
	}

	/**
	 * Gets the object-level constraint checks for the given class
	 *
	 * @param clazz the class to get the checks for
	 * @throws IllegalArgumentException if <code>clazz == null</code>
	 */
	public Check[] getChecks(final Class< ? > clazz) throws IllegalArgumentException
	{
		Assert.argumentNotNull("clazz", clazz);

		final ClassChecks cc = getClassChecks(clazz);

		final Set<Check> checks = cc.checksForObject;
		return checks == null ? null : checks.toArray(new Check[checks.size()]);
	}

	/**
	 * Gets the constraint checks for the given field
	 *
	 * @param field the field to get the checks for
	 * @throws IllegalArgumentException if <code>field == null</code>
	 */
	public Check[] getChecks(final Field field) throws IllegalArgumentException
	{
		Assert.argumentNotNull("field", field);

		final ClassChecks cc = getClassChecks(field.getDeclaringClass());

		final Set<Check> checks = cc.checksForFields.get(field);
		return checks == null ? null : checks.toArray(new Check[checks.size()]);
	}

	/**
	 * Gets the constraint checks for the given method's return value
	 *
	 * @param method the method to get the checks for
	 * @throws IllegalArgumentException if <code>getter == null</code>
	 */
	public Check[] getChecks(final Method method) throws IllegalArgumentException
	{
		Assert.argumentNotNull("method", method);

		final ClassChecks cc = getClassChecks(method.getDeclaringClass());

		final Set<Check> checks = cc.checksForMethodReturnValues.get(method);
		return checks == null ? null : checks.toArray(new Check[checks.size()]);
	}

	/**
	 * Returns the ClassChecks object for the particular class,
	 * allowing you to modify the checks
	 *
	 * @param clazz cannot be null
	 * @return returns the ClassChecks for the given class
	 * @throws IllegalArgumentException if <code>clazz == null</code>
	 */
	protected ClassChecks getClassChecks(final Class< ? > clazz) throws IllegalArgumentException, InvalidConfigurationException,
	ReflectionException
	{
		Assert.argumentNotNull("clazz", clazz);

		synchronized (checksByClass)
		{
			ClassChecks cc = checksByClass.get(clazz);

			if (cc == null)
			{
				cc = new ClassChecks(clazz, parameterNameResolver);

				for (final Configurer configurer : configurers)
				{
					final ClassConfiguration classConfig = configurer.getClassConfiguration(clazz);
					if (classConfig != null)
					{
						_addChecks(cc, classConfig);
					}
				}

				checksByClass.put(clazz, cc);
			}

			return cc;
		}
	}

	/**
	 * @return the internal linked set with the registered configurers
	 */
	public Set<Configurer> getConfigurers()
	{
		return configurers;
	}

	/**
	 * Returns the given constraint set.
	 *
	 * @param constraintSetId the id of the constraint set to retrieve
	 * @return the constraint set or null if not found
	 * @throws InvalidConfigurationException
	 * @throws IllegalArgumentException if <code>constraintSetId</code> is null
	 */
	public ConstraintSet getConstraintSet(final String constraintSetId) throws InvalidConfigurationException, IllegalArgumentException
	{
		Assert.argumentNotNull("constraintSetsById", constraintSetsById);
		synchronized (constraintSetsById)
		{
			ConstraintSet cs = constraintSetsById.get(constraintSetId);

			if (cs == null)
			{
				for (final Configurer configurer : configurers)
				{
					final ConstraintSetConfiguration csc = configurer.getConstraintSetConfiguration(constraintSetId);
					if (csc != null)
					{
						cs = new ConstraintSet(csc.id);
						cs.setChecks(csc.checks);

						addConstraintSet(cs, csc.overwrite);
					}
				}
			}
			return cs;
		}
	}

	/**
	 * @return the exceptionProcessor
	 */
	public ExceptionTranslator getExceptionTranslator()
	{
		return exceptionTranslator;
	}

	/**
	 * @return the expressionLanguageRegistry
	 */
	public ExpressionLanguageRegistry getExpressionLanguageRegistry()
	{
		return expressionLanguageRegistry;
	}

	/**
	 * @return the objectGraphNavigatorRegistry
	 */
	public ObjectGraphNavigatorRegistry getObjectGraphNavigatorRegistry()
	{
		return ognRegistry;
	}

	/**
	 * Determines if at least one of the given profiles is enabled
	 *
	 * @param profilesOfCheck
	 * @param enabledProfiles optional array of profiles (can be null)
	 * @return Returns true if at least one of the given profiles is enabled.
	 */
	protected boolean isAnyProfileEnabled(final String[] profilesOfCheck, final String[] enabledProfiles)
	{
		if (enabledProfiles == null)
		{
			// use the global profile configuration
			if (profilesOfCheck == null || profilesOfCheck.length == 0) return isProfileEnabled("default");

			for (final String profile : profilesOfCheck)
				if (isProfileEnabled(profile)) return true;
		}
		else
		{
			// use the local profile configuration
			if (profilesOfCheck == null || profilesOfCheck.length == 0) return ArrayUtils.containsEqual(enabledProfiles, "default");

			for (final String profile : profilesOfCheck)
				if (ArrayUtils.containsEqual(enabledProfiles, profile)) return true;
		}
		return false;
	}

	/**
	 * Determines if the given object is currently validated in the current thread
	 *
	 * @param object
	 * @return Returns true if the given object is currently validated in the current thread.
	 */
	protected boolean isCurrentlyValidated(final Object object)
	{
		Assert.argumentNotNull("object", object);
		return currentlyValidatedObjects.get().getLast().contains(object);
	}

	/**
	 * Determines if the given profile is enabled.
	 *
	 * @param profileId
	 * @return Returns true if the given profile is enabled.
	 */
	public boolean isProfileEnabled(final String profileId)
	{
		Assert.argumentNotNull("profileId", profileId);
		if (isProfilesFeatureUsed)
		{
			if (isAllProfilesEnabledByDefault) return !disabledProfiles.contains(profileId);

			return enabledProfiles.contains(profileId);
		}
		return true;
	}

	/**
	 * clears the checks and constraint sets => a reconfiguration using the
	 * currently registered configurers will automatically happen
	 */
	public void reconfigureChecks()
	{
		synchronized (checksByClass)
		{
			checksByClass.clear();
		}
		synchronized (constraintSetsById)
		{
			constraintSetsById.clear();
		}
	}

	/**
	 * Removes object-level constraint checks
	 *
	 * @param clazz
	 * @param checks
	 * @throws IllegalArgumentException if <code>clazz == null</code> or <code>checks == null</code> or checks is empty
	 */
	public void removeChecks(final Class< ? > clazz, final Check... checks) throws IllegalArgumentException
	{
		Assert.argumentNotNull("clazz", clazz);
		Assert.argumentNotEmpty("checks", checks);

		getClassChecks(clazz).removeObjectChecks(checks);
	}

	/**
	 * Removes constraint checks for the given field
	 *
	 * @param field
	 * @param checks
	 * @throws IllegalArgumentException if <code>field == null</code> or <code>checks == null</code> or checks is empty
	 */
	public void removeChecks(final Field field, final Check... checks) throws IllegalArgumentException
	{
		Assert.argumentNotNull("field", field);
		Assert.argumentNotEmpty("checks", checks);

		getClassChecks(field.getDeclaringClass()).removeFieldChecks(field, checks);
	}

	/**
	 * Removes constraint checks for the given getter's return value
	 *
	 * @param getter a JavaBean Getter style method
	 * @param checks
	 * @throws IllegalArgumentException if <code>getter == null</code> or <code>checks == null</code>
	 */
	public void removeChecks(final Method getter, final Check... checks) throws IllegalArgumentException
	{
		Assert.argumentNotNull("getter", getter);
		Assert.argumentNotEmpty("checks", checks);

		getClassChecks(getter.getDeclaringClass()).removeMethodReturnValueChecks(getter, checks);
	}

	/**
	 * Removes the constraint set with the given id
	 * @param id the id of the constraint set to remove, cannot be null
	 * @return the removed constraint set
	 * @throws IllegalArgumentException if <code>id == null</code>
	 */
	public ConstraintSet removeConstraintSet(final String id) throws IllegalArgumentException
	{
		Assert.argumentNotNull("id", id);

		synchronized (constraintSetsById)
		{
			return constraintSetsById.remove(id);
		}
	}

	protected String renderMessage(final OValContext context, final Object value, final String messageKey,
			final Map<String, ? > messageValues)
	{
		String message = MessageRenderer.renderMessage(messageKey, messageValues);

		// if there are no place holders in the message simply return it
		if (message.indexOf('{') == -1) return message;

		message = StringUtils.replaceAll(message, "{context}", contextRenderer.render(context));
		message = StringUtils.replaceAll(message, "{invalidValue}", messageValueFormatter.format(value));

		return message;
	}

	/**
	 * Reports an additional constraint violation for the current validation cycle.
	 * This method is intended to be executed by check implementations only.
	 * @param constraintViolation the constraint violation
	 */
	public void reportConstraintViolation(final ConstraintViolation constraintViolation)
	{
		Assert.argumentNotNull("constraintViolation", constraintViolation);
		if (currentViolations.get().size() == 0)
			throw new IllegalStateException("No active validation cycle found for the current thread.");
		currentViolations.get().getLast().add(constraintViolation);
	}

	/**
	 * @param validatedObject may be null for static fields
	 */
	protected Object resolveValue(final FieldContext ctx, final Object validatedObject)
	{
		return ReflectionUtils.getFieldValue(ctx.getField(), validatedObject);
	}

	/**
	 * @param validatedObject may be null for static methods
	 */
	protected Object resolveValue(final MethodReturnValueContext ctx, final Object validatedObject)
	{
		return ReflectionUtils.invokeMethod(ctx.getMethod(), validatedObject);
	}

	/**
	 * @param exceptionTranslator the exceptionTranslator to set
	 */
	public void setExceptionTranslator(final ExceptionTranslator exceptionTranslator)
	{
		this.exceptionTranslator = exceptionTranslator;
	}

	protected RuntimeException translateException(final OValException ex)
	{
		if (exceptionTranslator != null)
		{
			final RuntimeException rex = exceptionTranslator.translateException(ex);
			if (rex != null) return rex;
		}
		return ex;
	}

	/**
	 * {@inheritDoc}
	 */
	public List<ConstraintViolation> validate(final Object validatedObject) throws IllegalArgumentException, ValidationFailedException
	{
		Assert.argumentNotNull("validatedObject", validatedObject);

		// create required objects for this validation cycle
		final List<ConstraintViolation> violations = new ArrayList<>();
		currentViolations.get().add(violations);
		currentlyValidatedObjects.get().add(new IdentitySet<Object>(4));

		try
		{
			validateInvariants(validatedObject, violations, (String[]) null);
			return violations;
		}
		finally
		{
			// remove the validation cycle related objects
			currentViolations.get().removeLast();
			currentlyValidatedObjects.get().removeLast();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public List<ConstraintViolation> validate(final Object validatedObject, final String... profiles) throws IllegalArgumentException,
	ValidationFailedException
	{
		Assert.argumentNotNull("validatedObject", validatedObject);

		// create required objects for this validation cycle
		final List<ConstraintViolation> violations = new ArrayList<>();
		currentViolations.get().add(violations);
		currentlyValidatedObjects.get().add(new IdentitySet<Object>(4));

		try
		{
			validateInvariants(validatedObject, violations, profiles);
			return violations;
		}
		finally
		{
			// remove the validation cycle related objects
			currentViolations.get().removeLast();
			currentlyValidatedObjects.get().removeLast();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public List<ConstraintViolation> validateFieldValue(final Object validatedObject, final Field validatedField,
			final Object fieldValueToValidate) throws IllegalArgumentException, ValidationFailedException
			{
		Assert.argumentNotNull("validatedObject", validatedObject);
		Assert.argumentNotNull("validatedField", validatedField);

		// create required objects for this validation cycle
		final List<ConstraintViolation> violations = new ArrayList<>();
		currentViolations.get().add(violations);
		currentlyValidatedObjects.get().add(new IdentitySet<Object>(4));

		try
		{
			final ClassChecks cc = getClassChecks(validatedField.getDeclaringClass());
			final Collection<Check> checks = cc.checksForFields.get(validatedField);

			if (checks == null || checks.size() == 0) return violations;

			final FieldContext context = ContextCache.getFieldContext(validatedField);

			for (final Check check : checks)
			{
				checkConstraint(violations, check, validatedObject, fieldValueToValidate, context, null, false);
			}
			return violations;
		}
		catch (final OValException ex)
		{
			throw new ValidationFailedException("Field validation failed. Field: " + validatedField + " Validated object: "
					+ validatedObject, ex);
		}
		finally
		{
			// remove the validation cycle related objects
			currentViolations.get().removeLast();
			currentlyValidatedObjects.get().removeLast();
		}
			}

	/**
	 * validates the field and getter constrains of the given object.
	 * if the given object is a class the static fields and getters
	 * are validated.
	 *
	 * @param validatedObject the object to validate, cannot be null
	 * @throws ValidationFailedException
	 * @throws IllegalArgumentException if <code>validatedObject == null</code>
	 */
	protected void validateInvariants(final Object validatedObject, final List<ConstraintViolation> violations, final String[] profiles)
			throws IllegalArgumentException, ValidationFailedException
	{
		Assert.argumentNotNull("validatedObject", validatedObject);

		currentlyValidatedObjects.get().getLast().add(validatedObject);
		if (validatedObject instanceof Class< ? >)
		{
			_validateStaticInvariants((Class< ? >) validatedObject, violations, profiles);
		}
		else
		{
			_validateObjectInvariants(validatedObject, validatedObject.getClass(), violations, profiles);
		}
	}
}
