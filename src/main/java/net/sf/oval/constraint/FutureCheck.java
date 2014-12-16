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

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;

import net.sf.oval.ConstraintTarget;
import net.sf.oval.Validator;
import net.sf.oval.configuration.annotation.AbstractAnnotationCheck;
import net.sf.oval.context.OValContext;
import net.sf.oval.internal.util.*;

/**
 * @author Sebastian Thomschke
 */
public final class FutureCheck extends AbstractDateCheck<Future>
{
	private static final long serialVersionUID = 1L;

	@Override
	public void configure(final Future constraintAnnotation)
	{
		super.configure(constraintAnnotation);
		setTolerance(constraintAnnotation.tolerance());
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
	 * {@inheritDoc}
	 */
	boolean isSatisfied(final Object validatedObject, final Date valueToValidate, final OValContext context,
			final Validator validator)
	{

            final long now = System.currentTimeMillis() + getTolerance();
            return valueToValidate.getTime() > now;
	}

}
