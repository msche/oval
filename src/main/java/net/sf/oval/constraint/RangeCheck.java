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
import java.util.Map;

/**
 * @author Sebastian Thomschke
 */
public final class RangeCheck extends AbstractAnnotationCheck<Range>
{
	private static final long serialVersionUID = 1L;

	private double min = Double.MIN_VALUE;
	private double max = Double.MAX_VALUE;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void configure(final Range constraintAnnotation)
	{
		super.configure(constraintAnnotation);
		setMax(constraintAnnotation.max());
		setMin(constraintAnnotation.min());
	}

    /**
     * Returns value object {@code ConstraintAnnotationSettings} containing the basic settings of the constraint annotation.
     *
     * @param constraintAnnotation Annotation from which the settings will be extracted
     *
     * @return Value object {@code ConstraintAnnotationSettings}.
     */
    protected ConstraintAnnotationSettings getSettings(final  Range constraintAnnotation) {

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
		messageVariables.put("max", Double.toString(max));
		messageVariables.put("min", Double.toString(min));
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
	 * @return the max
	 */
	public double getMax()
	{
		return max;
	}

	/**
	 * @return the min
	 */
	public double getMin()
	{
		return min;
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
			return doubleValue >= min && doubleValue <= max;
		}

		final String stringValue = valueToValidate.toString();
		try
		{
			final double doubleValue = Double.parseDouble(stringValue);
			return doubleValue >= min && doubleValue <= max;
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

	/**
	 * @param min the min to set
	 */
	public void setMin(final double min)
	{
		this.min = min;
		requireMessageVariablesRecreation();
	}
}
