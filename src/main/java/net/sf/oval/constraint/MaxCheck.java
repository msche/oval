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

import java.util.LinkedHashMap;
import java.util.Map;

import net.sf.oval.ConstraintTarget;
import net.sf.oval.Validator;
import net.sf.oval.configuration.annotation.AbstractAnnotationCheck;
import net.sf.oval.configuration.annotation.ConstraintAnnotationSettings;
import net.sf.oval.context.OValContext;

/**
 * @author Sebastian Thomschke
 */
public class MaxCheck extends AbstractAnnotationCheck<Max>
{
	private static final long serialVersionUID = 1L;

	private double max;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void configure(final Max constraintAnnotation)
	{
		super.configure(constraintAnnotation);
		setMax(constraintAnnotation.value());
	}

	/**
	 * {@inheritDoc}
	 */

	@Override
	protected Map<String, String> createMessageVariables()
	{
		final Map<String, String> messageVariables = new LinkedHashMap<>(2);
		messageVariables.put("max", Double.toString(max));
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
     * Returns value object {@code ConstraintAnnotationSettings} containing the basic settings of the constraint settings
     *
     * @param constraintAnnotation Annotation from which the settings will be extracted
     *
     * @return Value object {@code ConstraintAnnotationSettings}.
     */
    protected final ConstraintAnnotationSettings getSettings(final  Max constraintAnnotation) {

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
	 * @return the max
	 */
	public double getMax()
	{
		return max;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isSatisfied(final Object validatedObject, final Object valueToValidate, final OValContext context,
			final Validator validator)
	{
		if (valueToValidate == null) return true;

		if (valueToValidate instanceof Number)
		{
			final double doubleValue = ((Number) valueToValidate).doubleValue();
			return doubleValue <= max;
		}

		final String stringValue = valueToValidate.toString();
		try
		{
			final double doubleValue = Double.parseDouble(stringValue);
			return doubleValue <= max;
		}
		catch (final NumberFormatException e)
		{
			return false;
		}
	}

	/**
	 * @param max the max to set
	 */
	public void setMax(final double max)
	{
		this.max = max;
		requireMessageVariablesRecreation();
	}
}
