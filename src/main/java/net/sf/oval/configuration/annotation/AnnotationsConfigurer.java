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
 *     Chris Pheby - interface based method parameter validation (inspectInterfaces)
 *******************************************************************************/
package net.sf.oval.configuration.annotation;

import net.sf.oval.Check;
import net.sf.oval.configuration.CheckInitializationListener;
import net.sf.oval.configuration.Configurer;
import net.sf.oval.configuration.pojo.elements.ClassConfiguration;
import net.sf.oval.configuration.pojo.elements.ConstraintSetConfiguration;
import net.sf.oval.configuration.pojo.elements.ConstructorConfiguration;
import net.sf.oval.configuration.pojo.elements.FieldChecks;
import net.sf.oval.configuration.pojo.elements.MethodConfiguration;
import net.sf.oval.configuration.pojo.elements.ParameterChecks;
import net.sf.oval.exception.OValException;
import net.sf.oval.exception.ReflectionException;
import net.sf.oval.guard.Guarded;
import net.sf.oval.internal.util.Assert;
import net.sf.oval.internal.util.ReflectionUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Configurer that configures constraints based on annotations tagged with {@link Constraint}
 *
 * @author Sebastian Thomschke
 */
public class AnnotationsConfigurer implements Configurer
{
	protected final Set<CheckInitializationListener> listeners = new LinkedHashSet<CheckInitializationListener>(2);

	private List<ParameterChecks> createParameterChecks(final Annotation[][] paramAnnotations,
														final Class<?>[] parameterTypes)
	{
		final List<ParameterChecks> paramCfg = new ArrayList<>();

		// loop over all parameters of the current constructor
		for (int i = 0; i < paramAnnotations.length; i++)
		{
			final ParameterChecks pc = new ParameterChecks(parameterTypes[i]);

			// loop over all annotations of the current constructor parameter
			for (final Annotation annotation : paramAnnotations[i])
				// check if the current annotation is a constraint annotation
				if (annotation.annotationType().isAnnotationPresent(Constraint.class))
					pc.addCheck(initializeCheck(annotation));
				else if (annotation.annotationType().isAnnotationPresent(Constraints.class))
					pc.addChecks(initializeChecks(annotation));

			paramCfg.add(pc);
		}
		return paramCfg;
	}

	public boolean addCheckInitializationListener(final CheckInitializationListener listener)
	{
		Assert.argumentNotNull("listener", "[listener] must not be null");
		return listeners.add(listener);
	}

	protected void configureConstructorParameterChecks(final ClassConfiguration classCfg)
	{
		for (final Constructor< ? > constructor : classCfg.getType().getDeclaredConstructors())
		{
			final List<ParameterChecks> paramChecks = createParameterChecks(constructor.getParameterAnnotations(),
					constructor.getParameterTypes());

			if (paramChecks.size() > 0)
			{
				final ConstructorConfiguration cc = new ConstructorConfiguration(constructor, paramChecks);
				classCfg.addChecks(cc);
			}
		}
	}

	protected void configureFieldChecks(final ClassConfiguration classCfg)
	{

		for (final Field field : classCfg.getType().getDeclaredFields())
		{
			final FieldChecks fc = new FieldChecks(field);

			// loop over all annotations of the current field
			for (final Annotation annotation : field.getAnnotations())
				// check if the current annotation is a constraint annotation
				if (annotation.annotationType().isAnnotationPresent(Constraint.class))
					fc.addCheck(initializeCheck(annotation));
				else if (annotation.annotationType().isAnnotationPresent(Constraints.class))
					fc.addChecks(initializeChecks(annotation));

			if (fc.hasChecks()) {
				classCfg.addChecks(fc);
			}
		}
	}

	/**
	 * configure method return value and parameter checks
	 */
	protected void configureMethodChecks(final ClassConfiguration classCfg)
	{

		for (final Method method : classCfg.getType().getDeclaredMethods())
		{

			// loop over all annotations method and
			List<Check> returnValueChecks = new ArrayList<>();
			for (final Annotation annotation : ReflectionUtils.getAnnotations(method, classCfg.inspectInterfaces))
				if (annotation.annotationType().isAnnotationPresent(Constraint.class))
					returnValueChecks.add(initializeCheck(annotation));
				else if (annotation.annotationType().isAnnotationPresent(Constraints.class))
					returnValueChecks.addAll(initializeChecks(annotation));

			/*
			 * determine parameter checks
			 */
			final List<ParameterChecks> paramChecks = createParameterChecks(
					ReflectionUtils.getParameterAnnotations(method, classCfg.inspectInterfaces),
					method.getParameterTypes());

			// check if anything has been configured for this method at all
			if (paramChecks.size() > 0 || returnValueChecks.size() > 0)
			{
				final MethodConfiguration mc = new MethodConfiguration(
						method,
						ReflectionUtils.isAnnotationPresent(method, IsInvariant.class, classCfg.inspectInterfaces),
						paramChecks,
						returnValueChecks);
				classCfg.addChecks(mc);
			}
		}
	}

	protected void configureObjectLevelChecks(final ClassConfiguration classCfg)
	{
		final List<Check> checks = new ArrayList<>(2);

		for (final Annotation annotation : ReflectionUtils.getAnnotations(classCfg.getType(), classCfg.inspectInterfaces))
			// check if the current annotation is a constraint annotation
			if (annotation.annotationType().isAnnotationPresent(Constraint.class))
				checks.add(initializeCheck(annotation));
			else if (annotation.annotationType().isAnnotationPresent(Constraints.class))
				checks.addAll(initializeChecks(annotation));

		if (checks.size() > 0)
		{
			classCfg.getInstanceChecks().addChecks(checks);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public ClassConfiguration getClassConfiguration(final Class< ? > clazz)
	{
		final ClassConfiguration classCfg = new ClassConfiguration(clazz);

		final Guarded guarded = clazz.getAnnotation(Guarded.class);

		if (guarded == null)
		{
			classCfg.applyFieldConstraintsToConstructors = false;
			classCfg.applyFieldConstraintsToSetters = false;
			classCfg.inspectInterfaces = false;
		}
		else
		{
			classCfg.applyFieldConstraintsToConstructors = guarded.applyFieldConstraintsToConstructors();
			classCfg.applyFieldConstraintsToSetters = guarded.applyFieldConstraintsToSetters();
			classCfg.inspectInterfaces = guarded.inspectInterfaces();
		}

		configureObjectLevelChecks(classCfg);
		configureFieldChecks(classCfg);
		configureConstructorParameterChecks(classCfg);
		configureMethodChecks(classCfg);

		return classCfg;
	}

	/**
	 * {@inheritDoc}
	 */
	public ConstraintSetConfiguration getConstraintSetConfiguration(final String constraintSetId)
	{
		return null;
	}

	protected <ConstraintAnnotation extends Annotation> AnnotationCheck<ConstraintAnnotation> initializeCheck(
			final ConstraintAnnotation constraintAnnotation) throws ReflectionException
	{
		assert constraintAnnotation != null;

		final Constraint constraint = constraintAnnotation.annotationType().getAnnotation(Constraint.class);

		// determine the check class
		@SuppressWarnings("unchecked")
		final Class<AnnotationCheck<ConstraintAnnotation>> checkClass = (Class<AnnotationCheck<ConstraintAnnotation>>) constraint
				.validatedBy();

		// instantiate the appropriate check for the found constraint
		final AnnotationCheck<ConstraintAnnotation> check = newCheckInstance(checkClass);
		check.configure(constraintAnnotation);

		for (final CheckInitializationListener listener : listeners)
			listener.onCheckInitialized(check);
		return check;
	}

	protected <ConstraintAnnotation extends Annotation>  List<Check>  initializeChecks(final ConstraintAnnotation constraintsAnnotation) throws ReflectionException
	{
		final List<Check> checks = new ArrayList();
		try
		{
			final Method getValue = constraintsAnnotation.annotationType().getDeclaredMethod("value", (Class< ? >[]) null);
			final Object[] constraintAnnotations = (Object[]) getValue.invoke(constraintsAnnotation, (Object[]) null);
			for (final Object ca : constraintAnnotations)
				checks.add(initializeCheck((Annotation) ca));
		}
		catch (final ReflectionException ex)
		{
			throw ex;
		}
		catch (final Exception ex)
		{
			throw new ReflectionException("Cannot initialize constraint check " + constraintsAnnotation.annotationType().getName(), ex);
		}
		return checks;
	}

	/**
	 * @return a new instance of the given constraint check implementation class
	 */
	protected <ConstraintAnnotation extends Annotation> AnnotationCheck<ConstraintAnnotation> newCheckInstance(
			final Class<AnnotationCheck<ConstraintAnnotation>> checkClass) throws OValException
	{
		try
		{
			return checkClass.newInstance();
		}
		catch (final InstantiationException ex)
		{
			throw new ReflectionException("Cannot initialize constraint check " + checkClass.getName(), ex);
		}
		catch (final IllegalAccessException ex)
		{
			throw new ReflectionException("Cannot initialize constraint check " + checkClass.getName(), ex);
		}
	}

	public boolean removeCheckInitializationListener(final CheckInitializationListener listener)
	{
		return listeners.remove(listener);
	}
}
