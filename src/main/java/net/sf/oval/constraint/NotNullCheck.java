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

import javax.validation.constraints.NotNull;

/**
 * @author Sebastian Thomschke
 */
public final class NotNullCheck extends AbstractAnnotationCheck<NotNull>
{
	private static final long serialVersionUID = 1L;

    /**
     * {@inheritDoc}
     */
    @Override
    public void configure(final NotNull constraintAnnotation)
    {
        setMessage(constraintAnnotation.message());
        setGroups(constraintAnnotation.groups());
    }

	/**
	 * {@inheritDoc}
	 */
	public boolean isSatisfied(final Object validatedObject, final Object valueToValidate, final OValContext context,
			final Validator validator)
	{
		return valueToValidate != null;
	}

}
