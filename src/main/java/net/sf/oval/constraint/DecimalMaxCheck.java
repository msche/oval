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
import net.sf.oval.exception.InvalidConfigurationException;
import net.sf.oval.internal.util.CollectionType;
import net.sf.oval.internal.util.CollectionUtils;

import javax.validation.constraints.DecimalMax;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author Sebastian Thomschke
 */
public final class DecimalMaxCheck extends AbstractAnnotationCheck<DecimalMax>
{
	private static final long serialVersionUID = 1L;

	private double max;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void configure(final DecimalMax constraintAnnotation)
	{
		setMessage(constraintAnnotation.message());
		setGroups(constraintAnnotation.groups());
		setMax(Double.parseDouble(constraintAnnotation.value()));
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

		if (CollectionUtils.getType(valueToValidate) == CollectionType.SINGLE) {
			if (valueToValidate instanceof Number) {
				final double doubleValue = ((Number) valueToValidate).doubleValue();
				System.out.println("Checking value " + doubleValue);
				return doubleValue <= max;
			}

			final String stringValue = valueToValidate.toString();
			System.out.println("Checking value " + stringValue);
			try {
				final double doubleValue = Double.parseDouble(stringValue);
				return doubleValue <= max;
			} catch (final NumberFormatException e) {
				System.out.println("Unable to convert value to double");
				return false;
			}
		} else {
			throw new InvalidConfigurationException("Constraint can only be applied to single values");
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
