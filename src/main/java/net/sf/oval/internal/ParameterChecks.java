/*******************************************************************************
 * Portions created by Sebastian Thomschke are copyright (c) 2005-2013 Sebastian
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
package net.sf.oval.internal;

import net.sf.oval.Check;
import net.sf.oval.context.ConstructorParameterContext;
import net.sf.oval.context.MethodParameterContext;
import net.sf.oval.context.OValContext;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Contains check(s) for parameter of Method/Constructor.
 *
 * @author Sebastian Thomschke
 * @author msche
 */
public final class ParameterChecks
{
	/**
	 * Contains set of checks that apply to parameter.
	 */
	private final Set<Check> checks = new LinkedHashSet();

	/**
	 * Contains context of checks
	 */
	private final OValContext context;

	/**
	 * Constructor parameter checks
	 *
	 * @param constructor {@code Constructor} at which the parameter checks applies
	 * @param paramIndex index of parameter at which the checks apply
	 * @param paramName name of parameter at which the checks apply
	 */
	ParameterChecks(final Constructor< ? > constructor, final int paramIndex, final String paramName)
	{
		context = new ConstructorParameterContext(constructor, paramIndex, paramName);
	}

	/**
	 * Method parameter checks
	 *
	 * @param method {@code Method} at which the parameter checks applies
	 * @param paramIndex index of parameter at which the checks apply
	 * @param paramName name of parameter at which the checks apply
	 */
	ParameterChecks(final Method method, final int paramIndex, final String paramName)
	{
		context = new MethodParameterContext(method, paramIndex, paramName);
	}

	/**
	 * Returns whether there are checks for this parameter.
	 *
	 * @return true if there are checks specified; false otherwise.
	 */
	public boolean hasChecks()
	{
		return checks.size() > 0;
	}

	/**
	 * Appends the specified checks to the existing parameter checks
	 *
	 * @param newChecks checks that need to be appended
	 */
	public void addChecks(final Collection<Check> newChecks) {
		for (Check check : newChecks) {
			if (check.getContext()==null) {
				check.setContext(context);
			}
		}
		checks.addAll(newChecks);
	}

	/**
	 * Removes specified check from existing parameter checks
	 *
	 * param check check that needs to be removed
	 */
	public void removeCheck(final Check check) {
		checks.remove(check);
	}

	/**
	 * Returns checks that apply to parameter
	 *
	 * @return unmodifiable set of checks that apply to parameter
	 */
	public Set<Check> getChecks() {
		return Collections.unmodifiableSet(checks);
	}

}
