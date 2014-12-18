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
package net.sf.oval.guard;

import net.sf.oval.ConstraintViolation;
import net.sf.oval.exception.ConstraintsViolatedException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Sebastian Thomschke
 */
public class ConstraintsViolatedAdapter implements ConstraintsViolatedListener
{
	private final List<ConstraintsViolatedException> violationExceptions = new ArrayList<>(8);
	private final List<ConstraintViolation> violations = new ArrayList<>(8);

	public void clear()
	{
		violationExceptions.clear();
		violations.clear();
	}

	/**
	 * @return Returns the constraint violation exceptions.
	 */
	public List<ConstraintsViolatedException> getConstraintsViolatedExceptions()
	{
		return violationExceptions;
	}

	/**
	 * @return Returns the constraint violations.
	 */
	public List<ConstraintViolation> getConstraintViolations()
	{
		return violations;
	}

	/**
	 * {@inheritDoc}
	 */
	public void onConstraintsViolatedException(final ConstraintsViolatedException exception)
	{
		violationExceptions.add(exception);
		violations.addAll(Arrays.asList(exception.getConstraintViolations()));
	}
}
