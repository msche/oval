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

import net.sf.oval.context.OValContext;

import javax.validation.constraints.DecimalMin;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author Sebastian Thomschke
 */
public final class DecimalMinCheck extends AbstractAnnotationCheck<DecimalMin>
{
	private static final long serialVersionUID = 1L;

	private double min;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void configure(final DecimalMin constraintAnnotation)
	{
		setMessage(constraintAnnotation.message());
		setGroups(constraintAnnotation.groups());
		setMin(Double.parseDouble(constraintAnnotation.value()));
	}

    /**
	 * {@inheritDoc}
	 */
	@Override
	protected Map<String, String> createMessageVariables()
	{
		final Map<String, String> messageVariables = new LinkedHashMap<>(2);
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
			return doubleValue >= min;
		}

		final String stringValue = valueToValidate.toString();
		try
		{
			final double doubleValue = Double.parseDouble(stringValue);
			return doubleValue >= min;
		}
		catch (final NumberFormatException e)
		{
			return false;
		}
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
