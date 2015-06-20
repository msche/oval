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
public final class ReturnValueChecks extends AbstractChecks {
    /**
     * Constructor check return value
     *
     * @param check check that will be applied to return value
     */
    public ReturnValueChecks(Class<?> type, Check check) {
        super(type);
        addCheck(check);
    }

    /**
     * Constructor check return value
     *
     * @param checks checks that will be applied to return value
     */
    public ReturnValueChecks(Class<?> type, List<Check> checks) {
        super(type);
        addChecks(checks);
    }

}
