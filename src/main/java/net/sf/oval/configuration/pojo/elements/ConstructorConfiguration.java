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

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Contains checks that should be applied to constructor of class.
 *
 * @author Sebastian Thomschke
 * @author msche
 */
public class ConstructorConfiguration
{
	private static final long serialVersionUID = 1L;

	/**
	 * Constructor at which this configuration applies
	 */
	private final Constructor constructor;

	/**
	 * Checks for parameters constructor
	 */
	private final List<ParameterChecks> parameterChecks;

	public ConstructorConfiguration(Constructor constructor, List<ParameterChecks> checks) {
		Assert.argumentNotNull("constructor", constructor);
		Assert.argumentNotNull("checks", checks);

		this.constructor = constructor;
		parameterChecks = checks;
	}

	/**
	 * Return constructor at which this configuration applies
	 */
	public Constructor getConstructor() {
		return constructor;
	}

	/**
	 * Returns checks that should be applied to constructor parameters
	 */
	public List<ParameterChecks> getParameterChecks() {
		return Collections.unmodifiableList(parameterChecks);
	}
}
