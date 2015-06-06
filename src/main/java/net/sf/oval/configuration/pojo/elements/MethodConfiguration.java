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

import net.sf.oval.internal.util.Assert;

import java.util.Collections;
import java.util.List;

/**
 * @author Sebastian Thomschke
 */
public class MethodConfiguration
{
	private static final long serialVersionUID = 1L;

	/**
	 * Name of method
	 */
	private final String name;

	private final boolean isInvariant;

	/**
	 * Contains the checks that should be applied to the method parameters.
	 */
	private final List<ParameterChecks> parameterChecks;

	/**
	 * Contains the checks that should be applied to the return value.
	 */
	private final ReturnValueChecks returnValueChecks;

	public MethodConfiguration(String name, boolean isInvariant, List<ParameterChecks> parameterChecks, ReturnValueChecks returnValueChecks) {

		Assert.argumentNotEmpty("name", name);
		Assert.argumentNotNull("parameterChecks", parameterChecks);
		Assert.argumentNotNull("returnValueChecks", returnValueChecks);

		this.name = name;
		this.isInvariant = isInvariant;
		this.parameterChecks = parameterChecks;
		this.returnValueChecks = returnValueChecks;
	}

	/**
	 * Returns name of method at which this configuration applies
	 */
	public String getName() {
		return name;
	}

	public boolean isInvariant() {
		return isInvariant;
	}

	/**
	 * Returns checks for parameters method
	 */
	public List<ParameterChecks> getParameterChecks() {
		return Collections.unmodifiableList(parameterChecks);
	}

	/**
	 * Returns checks for return value method
	 */
	public ReturnValueChecks getReturnValueChecks() {
		return returnValueChecks;
	}


}
