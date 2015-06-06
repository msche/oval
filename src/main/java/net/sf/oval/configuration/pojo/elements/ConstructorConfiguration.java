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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Sebastian Thomschke
 */
public class ConstructorConfiguration
{
	private static final long serialVersionUID = 1L;
	
	private final List<ParameterChecks> parameterChecks;

	public ConstructorConfiguration(List<ParameterChecks> checks) {
		Assert.argumentNotNull("checks", checks);
		parameterChecks = checks;
	}

	public List<ParameterChecks> getParameterChecks() {
		return Collections.unmodifiableList(parameterChecks);
	}
}
