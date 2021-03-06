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
package net.sf.oval.constraint;

import net.sf.oval.Validator;
import net.sf.oval.configuration.annotation.AbstractAnnotationCheck;
import net.sf.oval.configuration.annotation.ConstraintAnnotationSettings;
import net.sf.oval.context.OValContext;
import net.sf.oval.exception.ReflectionException;
import net.sf.oval.internal.util.Assert;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author Sebastian Thomschke
 */
public final class CheckWithCheck extends AbstractAnnotationCheck<CheckWith>
{
	public interface SimpleCheck extends Serializable
	{
		boolean isSatisfied(Object validatedObject, Object value);
	}

	public interface SimpleCheckWithMessageVariables extends SimpleCheck
	{
		Map<String, ? extends Serializable> createMessageVariables();
	}

	private static final long serialVersionUID = 1L;

	private boolean ignoreIfNull;
	private SimpleCheck simpleCheck;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void configure(final CheckWith constraintAnnotation)
	{
		super.configure(constraintAnnotation);
		setSimpleCheck(constraintAnnotation.value());
		setIgnoreIfNull(constraintAnnotation.ignoreIfNull());
	}

    /**
     * Returns value object {@code ConstraintAnnotationSettings} containing the basic settings of the constraint annotations
     *
     * @param constraintAnnotation Annotation from which the settings will be extracted
     *
     * @return Value object {@code ConstraintAnnotationSettings}.
     */
    protected ConstraintAnnotationSettings getSettings(final CheckWith constraintAnnotation) {

        ConstraintAnnotationSettings settings = new ConstraintAnnotationSettings.Builder()
                .message(constraintAnnotation.message())
                .appliesTo(constraintAnnotation.appliesTo())
                .errorCode(constraintAnnotation.errorCode())
                .severity(constraintAnnotation.severity())
                .profiles(constraintAnnotation.profiles())
                .target(constraintAnnotation.target())
                .when(constraintAnnotation.when())
                .build();
        return settings;
    }
	/**
	 * {@inheritDoc}
	 */
	@Override
	public Map<String, ? extends Serializable> createMessageVariables()
	{
		final Map<String, Serializable> messageVariables = new LinkedHashMap<>(4);

		if (simpleCheck instanceof SimpleCheckWithMessageVariables)
		{
			final Map<String, ? extends Serializable> simpleCheckMessageVariables = ((SimpleCheckWithMessageVariables) simpleCheck)
					.createMessageVariables();
			if (simpleCheckMessageVariables != null) messageVariables.putAll(simpleCheckMessageVariables);
		}
		messageVariables.put("ignoreIfNull", Boolean.toString(ignoreIfNull));
		messageVariables.put("simpleCheck", simpleCheck.getClass().getName());
		return messageVariables;
	}

	/**
	 * @return the simpleCheck
	 */
	public SimpleCheck getSimpleCheck()
	{
		return simpleCheck;
	}

	/**
	 * @return the ignoreIfNull
	 */
	public boolean isIgnoreIfNull()
	{
		return ignoreIfNull;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isSatisfied(final Object validatedObject, final Object valueToValidate, final OValContext context,
			final Validator validator) throws ReflectionException
	{
		if (valueToValidate == null && ignoreIfNull) return true;

		return simpleCheck.isSatisfied(validatedObject, valueToValidate);
	}

	/**
	 * @param ignoreIfNull the ignoreIfNull to set
	 */
	public void setIgnoreIfNull(final boolean ignoreIfNull)
	{
		this.ignoreIfNull = ignoreIfNull;
		requireMessageVariablesRecreation();
	}

	/**
	 * @param simpleCheckType the simpleCheckType to set
	 * @throws IllegalArgumentException if <code>simpleCheckType == null</code>
	 */
	public void setSimpleCheck(final Class< ? extends SimpleCheck> simpleCheckType) throws ReflectionException,
			IllegalArgumentException
	{
		Assert.argumentNotNull("simpleCheckType", simpleCheckType);

		try
		{
			final Constructor< ? extends SimpleCheck> ctor = simpleCheckType
					.getDeclaredConstructor((Class< ? >[]) null);
			ctor.setAccessible(true);
			simpleCheck = ctor.newInstance();
		}
		catch (final Exception ex)
		{
			throw new ReflectionException("Cannot instantiate an object of type  " + simpleCheckType.getName(), ex);
		}
		requireMessageVariablesRecreation();
	}

	/**
	 * @param simpleCheck the simpleCheck to set
	 * @throws IllegalArgumentException if <code>simpleCheck == null</code>
	 */
	public void setSimpleCheck(final SimpleCheck simpleCheck) throws IllegalArgumentException
	{
		Assert.argumentNotNull("simpleCheck", simpleCheck);

		this.simpleCheck = simpleCheck;
		requireMessageVariablesRecreation();
	}
}
