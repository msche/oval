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
 * Contains checks that apply to constructor
 *
 * @author Sebastian Thomschke
 * @author msche
 */
public final class ConstructorConfiguration extends ConfigurationElement
{
	private static final long serialVersionUID = 1L;

    /**
     * Contain checks that will be applied to constructor parameters.
     */
	private final List<ParameterChecks> parameterChecks;

    /**
     * Constructor
     *
     * @param checks checks that will be applied to parameters of constructor
     */
    public ConstructorConfiguration(List<ParameterChecks> checks) {
        Assert.argumentNotNull("checks", checks);
        parameterChecks = checks;
    }

    /**
     * Returns whether there are any checks for parameters of constructor
     */
    public boolean hasParameterChecks() {
        return !parameterChecks.isEmpty();
    }

    /**
     * Return checks that will be applied to parameters of constructor
     */
    public List<ParameterChecks> getParameterChecks() {
        return Collections.unmodifiableList(parameterChecks);
    }

}
