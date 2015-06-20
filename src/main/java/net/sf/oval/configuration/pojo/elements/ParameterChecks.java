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

/**
 * Contains checks for parameter
 *
 * @author Sebastian Thomschke
 * @author msche
 */
public final class ParameterChecks extends AbstractChecks {
    private static final long serialVersionUID = 1L;

    /**
     * Constructor
     *
     * @param type type of parameter
     */
    public ParameterChecks(Class<?> type) {
        super(type);
    }

}
