/*******************************************************************************
 * Portions created by Sebastian Thomschke are copyright (c) 2005-2011 Sebastian
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
package net.sf.oval.configuration.annotation;

import net.sf.oval.Check;
import net.sf.oval.configuration.Configurer;
import net.sf.oval.configuration.pojo.elements.ClassConfiguration;
import net.sf.oval.configuration.pojo.elements.ConstraintSetConfiguration;
import net.sf.oval.configuration.pojo.elements.ConstructorConfiguration;
import net.sf.oval.configuration.pojo.elements.FieldChecks;
import net.sf.oval.configuration.pojo.elements.MethodConfiguration;
import net.sf.oval.configuration.pojo.elements.ReturnValueChecks;
import net.sf.oval.configuration.pojo.elements.ParameterChecks;
import net.sf.oval.constraint.AssertFalseCheck;
import net.sf.oval.constraint.NullCheck;
import net.sf.oval.constraint.AssertTrueCheck;
import net.sf.oval.constraint.ValidCheck;
import net.sf.oval.constraint.DigitsCheck;
import net.sf.oval.constraint.FutureCheck;
import net.sf.oval.constraint.PatternCheck;
import net.sf.oval.constraint.DecimalMaxCheck;
import net.sf.oval.constraint.DecimalMinCheck;
import net.sf.oval.constraint.NotNullCheck;
import net.sf.oval.constraint.PastCheck;
import net.sf.oval.constraint.SizeCheck;
import net.sf.oval.guard.Guarded;
import net.sf.oval.internal.util.ReflectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.Valid;
import javax.validation.constraints.AssertFalse;
import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Digits;
import javax.validation.constraints.Future;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import javax.validation.constraints.Past;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Pattern.Flag;
import javax.validation.constraints.Size;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

/**
 * Constraints configurer that interprets the JSR303 built-in Java Bean Validation annotations:
 * <ul>
 * <li>javax.validation.constraints.AssertFalse    => net.sf.oval.constraint.AssertFalseCheck
 * <li>javax.validation.constraints.AssertTrue     => net.sf.oval.constraint.AssertTrueCheck
 * <li>javax.validation.constraints.DecimalMax     => net.sf.oval.constraint.MaxCheck
 * <li>javax.validation.constraints.DecimalMin     => net.sf.oval.constraint.MinCheck
 * <li>javax.validation.constraints.Digits         => net.sf.oval.constraint.DigitsCheck
 * <li>javax.validation.constraints.Future         => net.sf.oval.constraint.FutureCheck
 * <li>javax.validation.constraints.Max            => net.sf.oval.constraint.MaxCheck
 * <li>javax.validation.constraints.Min            => net.sf.oval.constraint.MinCheck
 * <li>javax.validation.constraints.Past           => net.sf.oval.constraint.PastCheck
 * <li>javax.validation.constraints.Pattern        => net.sf.oval.constraint.PatternCheck
 * <li>javax.validation.constraints.Size           => net.sf.oval.constraint.SizeCheck
 * </ul>
 * @author Sebastian Thomschke
 */
public class BeanValidationAnnotationsConfigurer implements Configurer
{
	private static final Logger LOG = LoggerFactory.getLogger(BeanValidationAnnotationsConfigurer.class);

	private List<ParameterChecks> createParameterChecks(final Annotation[][] paramAnnotations,
														final Class<?>[] parameterTypes)
	{
		final List<ParameterChecks> paramChecks = new ArrayList<>(paramAnnotations.length);

		// loop over all parameters of the current constructor
		for (int i = 0; i < paramAnnotations.length; i++)
		{
			final ParameterChecks pc = new ParameterChecks(parameterTypes[i]);

			// loop over all annotations of the current constructor parameter
			for (final Annotation annotation : paramAnnotations[i])
				pc.addChecks(initializeChecks(annotation));

			paramChecks.add(pc);
		}
		return paramChecks;
	}

	protected void configureConstructorParameterChecks(final ClassConfiguration classCfg)
	{
		for (final Constructor< ? > ctor : classCfg.getType().getDeclaredConstructors())
		{
			final List<ParameterChecks> paramChecks = createParameterChecks(ctor.getParameterAnnotations(),
					ctor.getParameterTypes());

			if (paramChecks.size() > 0)
			{
				final ConstructorConfiguration cc = new ConstructorConfiguration();
				cc.parameterChecks = paramChecks;
				classCfg.addChecks(cc);
			}
		}
	}

	protected void configureFieldChecks(final ClassConfiguration classCfg)
	{
        // Loop over all fields that are defined within the class.
		for (final Field field : classCfg.getType().getDeclaredFields())
		{
			final FieldChecks fc = new FieldChecks(field.getName());

			// loop over all annotations of the current field
			for (final Annotation annotation : field.getAnnotations())
				fc.addChecks(initializeChecks(annotation));

            // If checks defined for field append to field checks
			if (fc.hasChecks())
			{
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
			// loop over all annotations
			List<Check> returnValueChecks = new ArrayList<>(2);
			for (final Annotation annotation : ReflectionUtils.getAnnotations(method,classCfg.inspectInterfaces))
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
				final MethodConfiguration mc = new MethodConfiguration();
				mc.name = method.getName();
				mc.parameterChecks = paramChecks;
				mc.isInvariant = ReflectionUtils.isGetter(method);
				if (returnValueChecks.size() > 0)
				{
					mc.returnValueChecks = new ReturnValueChecks();
					mc.returnValueChecks.addChecks(returnValueChecks);
				}
				classCfg.addChecks(mc);
			}
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
			classCfg.assertParametersNotNull = false;
			classCfg.inspectInterfaces = false;
		}
		else
		{
			classCfg.applyFieldConstraintsToConstructors = guarded.applyFieldConstraintsToConstructors();
			classCfg.applyFieldConstraintsToSetters = guarded.applyFieldConstraintsToSetters();
			classCfg.assertParametersNotNull = guarded.assertParametersNotNull();
			classCfg.inspectInterfaces = guarded.inspectInterfaces();
		}

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

	protected List<Check>  initializeChecks(final Annotation annotation) {
		assert annotation != null;
		final List<Check> checks = new ArrayList();

		// ignore non-bean validation annotations
		if (!(annotation instanceof Valid) && annotation.annotationType().getAnnotation(javax.validation.Constraint.class) == null)
			return checks;

		Class< ? >[] groups = null;
		Check check = null;
		if (annotation instanceof NotNull)
		{
			groups = ((NotNull) annotation).groups();
			check = new NotNullCheck();
		}
		else if (annotation instanceof Null)
		{
			groups = ((Null) annotation).groups();
			check = new NullCheck();
		}
		else if (annotation instanceof Valid)
			check = new ValidCheck();
		else if (annotation instanceof AssertTrue)
		{
			groups = ((AssertTrue) annotation).groups();
			check = new AssertTrueCheck();
		}
		else if (annotation instanceof AssertFalse)
		{
			groups = ((AssertFalse) annotation).groups();
			check = new AssertFalseCheck();
		}
		else if (annotation instanceof DecimalMax)
		{
			groups = ((DecimalMax) annotation).groups();
			final DecimalMaxCheck maxCheck = new DecimalMaxCheck();
			maxCheck.setMax(Double.parseDouble(((DecimalMax) annotation).value()));
			check = maxCheck;
		}
		else if (annotation instanceof DecimalMin)
		{
			groups = ((DecimalMin) annotation).groups();
			final DecimalMinCheck minCheck = new DecimalMinCheck();
			minCheck.setMin(Double.parseDouble(((DecimalMin) annotation).value()));
			check = minCheck;
		}
		else if (annotation instanceof Max)
		{
			groups = ((Max) annotation).groups();
			final DecimalMaxCheck maxCheck = new DecimalMaxCheck();
			maxCheck.setMax(((Max) annotation).value());
			check = maxCheck;
		}
		else if (annotation instanceof Min)
		{
			groups = ((Min) annotation).groups();
			final DecimalMinCheck minCheck = new DecimalMinCheck();
			minCheck.setMin(((Min) annotation).value());
			check = minCheck;
		}
		else if (annotation instanceof Future)
		{
			groups = ((Future) annotation).groups();
			check = new FutureCheck();
		}
		else if (annotation instanceof Past)
		{
			groups = ((Past) annotation).groups();
			check = new PastCheck();
		}
		else if (annotation instanceof Pattern)
		{
			groups = ((Pattern) annotation).groups();
			final PatternCheck matchPatternCheck = new PatternCheck();
			int iflag = 0;
			for (final Flag flag : ((Pattern) annotation).flags())
				iflag = iflag | flag.getValue();
			matchPatternCheck.setPattern(((Pattern) annotation).regexp(), iflag);
			check = matchPatternCheck;
		}
		else if (annotation instanceof Digits)
		{
			groups = ((Digits) annotation).groups();
			final DigitsCheck digitsCheck = new DigitsCheck();
			digitsCheck.setMaxFraction(((Digits) annotation).fraction());
			digitsCheck.setMaxInteger(((Digits) annotation).integer());
			check = digitsCheck;
		}
		else if (annotation instanceof Size)
		{
			groups = ((Size) annotation).groups();
			final SizeCheck sizeCheck = new SizeCheck();
			sizeCheck.setMax(((Size) annotation).max());
			sizeCheck.setMin(((Size) annotation).min());
			check = sizeCheck;
		}

		if (check != null)
		{
			final Method getMessage = ReflectionUtils.getMethod(annotation.getClass(), "message", (Class< ? >[]) null);
			if (getMessage != null)
			{
				final String message = ReflectionUtils.invokeMethod(getMessage, annotation, (Object[]) null);
				if (message != null && !message.startsWith("{javax.validation.constraints.")) check.setMessage(message);
			}

			if (groups != null && groups.length > 0)
			{
				//final String[] profiles = new String[groups.length];
				//for (int i = 0, l = groups.length; i < l; i++)
				//	profiles[i] = groups[i].getCanonicalName();
				check.setGroups(groups);
			}
			checks.add(check);
			return checks;
		}

		Annotation[] list = null;
		if (annotation instanceof AssertFalse.List)
			list = ((AssertFalse.List) annotation).value();
		else if (annotation instanceof AssertTrue.List)
			list = ((AssertTrue.List) annotation).value();
		else if (annotation instanceof DecimalMax.List)
			list = ((DecimalMax.List) annotation).value();
		else if (annotation instanceof DecimalMin.List)
			list = ((DecimalMin.List) annotation).value();
		else if (annotation instanceof Digits.List)
			list = ((Digits.List) annotation).value();
		else if (annotation instanceof Future.List)
			list = ((Future.List) annotation).value();
		else if (annotation instanceof Max.List)
			list = ((Max.List) annotation).value();
		else if (annotation instanceof Min.List)
			list = ((Min.List) annotation).value();
		else if (annotation instanceof NotNull.List)
			list = ((NotNull.List) annotation).value();
		else if (annotation instanceof Null.List)
			list = ((Null.List) annotation).value();
		else if (annotation instanceof Past.List)
			list = ((Past.List) annotation).value();
		else if (annotation instanceof Pattern.List)
			list = ((Pattern.List) annotation).value();
		else if (annotation instanceof Size.List) list = ((Size.List) annotation).value();

		if (list != null)
			for (final Annotation anno : list)
				checks.addAll(initializeChecks(anno));
		else
		{
			LOG.warn("Ignoring unsupported bean validation constraint annotation {}", annotation);
		}
		return checks;

	}
}
