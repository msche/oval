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

import net.sf.oval.context.OValContext;
import net.sf.oval.internal.util.CollectionType;
import net.sf.oval.internal.util.CollectionUtils;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author Sebastian Thomschke
 */
public final class MinSizeCheck extends AbstractAnnotationCheck<MinSize>
{
	private static final long serialVersionUID = 1L;

	private int min;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void configure(final MinSize constraintAnnotation)
	{
		setMessage(constraintAnnotation.message());
		setGroups(constraintAnnotation.groups());
		setMin(constraintAnnotation.value());
	}

    /**
	 * {@inheritDoc}
	 */
	@Override
	protected Map<String, String> createMessageVariables()
	{
		final Map<String, String> messageVariables = new LinkedHashMap<>(2);
		messageVariables.put("min", Integer.toString(min));
		return messageVariables;
	}

	/**
	 * @return the min
	 */
	public int getMin()
	{
		return min;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isSatisfied(final Object validatedObject, final Object valueToValidate, final OValContext context,
			final Validator validator) {
		if (valueToValidate == null) {
			return true;
		} else {

			int size;
			switch (CollectionUtils.getType(valueToValidate)) {
				case COLLECTION:
					size = ((Collection<?>) valueToValidate).size();
					return size >= min;
				case MAP:
					size = ((Map<?, ?>) valueToValidate).size();
					return size >= min;
				case ARRAY:
					size = Array.getLength(valueToValidate);
					return size >= min;
				default:
					return false;

			}
		}
	}

	/**
	 * Verifies whether the type at which the check will be applied is supported
	 *
	 * @param type
	 */
	@Override
	public boolean supports(Class<?> type) {
		System.out.println(type + " resulted in " + CollectionUtils.getType(type) + "which caused return value " + (CollectionUtils.getType(type) != CollectionType.SINGLE));
		return CollectionUtils.getType(type) != CollectionType.SINGLE;
	}

	/**
	 * @param min the min to set
	 */
	public void setMin(final int min)
	{
		this.min = min;
		requireMessageVariablesRecreation();
	}
}
