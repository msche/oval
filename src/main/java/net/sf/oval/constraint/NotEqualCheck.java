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

import net.sf.oval.ConstraintTarget;
import net.sf.oval.Validator;
import net.sf.oval.configuration.annotation.AbstractAnnotationCheck;
import net.sf.oval.configuration.annotation.ConstraintAnnotationSettings;
import net.sf.oval.context.OValContext;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

/**
 * @author Sebastian Thomschke
 */
public final class NotEqualCheck extends AbstractAnnotationCheck<NotEqual>
{
	private static final long serialVersionUID = 1L;

	private boolean ignoreCase;
	private String testString;
	private transient String testStringLowerCase;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void configure(final NotEqual constraintAnnotation)
	{
		super.configure(constraintAnnotation);
		setIgnoreCase(constraintAnnotation.ignoreCase());
		setTestString(constraintAnnotation.value());
	}

    /**
     * Returns value object {@code ConstraintAnnotationSettings} containing the basic settings of the constraint annotation
     *
     * @param constraintAnnotation Annotation from which the settings will be extracted
     *
     * @return Value object {@code ConstraintAnnotationSettings}.
     */
    protected ConstraintAnnotationSettings getSettings(final  NotEqual constraintAnnotation) {

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
	protected Map<String, String> createMessageVariables()
	{
		final Map<String, String> messageVariables = new LinkedHashMap<>(2);
		messageVariables.put("ignoreCase", Boolean.toString(ignoreCase));
		messageVariables.put("testString", testString);
		return messageVariables;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected ConstraintTarget[] getAppliesToDefault()
	{
		return new ConstraintTarget[]{ConstraintTarget.VALUES};
	}

	/**
	 * @return the testString
	 */
	public String getTestString()
	{
		return testString;
	}

	private String getTestStringLowerCase()
	{
		if (testStringLowerCase == null && testString != null)
			testStringLowerCase = testString.toLowerCase(Locale.getDefault());
		return testStringLowerCase;
	}

	/**
	 * @return the ignoreCase
	 */
	public boolean isIgnoreCase()
	{
		return ignoreCase;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isSatisfied(final Object validatedObject, final Object valueToValidate, final OValContext context,
			final Validator validator)
	{
		if (valueToValidate == null) return true;

		if (ignoreCase)
			return !valueToValidate.toString().toLowerCase(Locale.getDefault()).equals(getTestStringLowerCase());

		return !valueToValidate.toString().equals(testString);
	}

	/**
	 * @param ignoreCase the ignoreCase to set
	 */
	public void setIgnoreCase(final boolean ignoreCase)
	{
		this.ignoreCase = ignoreCase;
		requireMessageVariablesRecreation();
	}

	/**
	 * @param testString the testString to set
	 */
	public void setTestString(final String testString)
	{
		this.testString = testString;
		requireMessageVariablesRecreation();
	}
}
