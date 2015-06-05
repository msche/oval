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
package net.sf.oval.configuration.pojo.elements;

import net.sf.oval.Check;

import java.util.ArrayList;
import java.util.List;

/**
 * Contains checks for return value of method
 *
 * @author Sebastian Thomschke
 * @author msche
 */
public final class ReturnValueChecks extends ConfigurationElement
{
	private static final long serialVersionUID = 1L;

	/**
	 * checks for a method's return value that need to be verified after method execution
	 */
	private final List<Check> checks = new ArrayList();

	/**
	 * Returns whether there are checks for the return value
	 */
	public boolean hasChecks() {
		return !checks.isEmpty();
	}

	/**
	 * Returns checks that apply to return value
	 */
	public List<Check> getChecks() {
		return checks;
	}

	/**
	 * Append check for return value
	 */
	public void addCheck(Check check) {
		checks.add(check);
	}

	/**
	 * Append checks for return value
	 */
	public void addChecks(List<Check> returnValueChecks) {
		checks.addAll(returnValueChecks);
	}
}
