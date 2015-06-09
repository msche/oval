/*******************************************************************************
 * Portions created by Sebastian Thomschke are copyright (c) 2005-2010 Sebastian
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
package net.sf.oval.test.constraints;

import junit.framework.TestCase;
import net.sf.oval.Check;
import net.sf.oval.Validator;

/**
 * @author Sebastian Thomschke
 */
public abstract class AbstractContraintsTest extends TestCase
{
	protected final Validator validator = new Validator();

	/**
	 * Performs basic tests of the check implementation.
	 * @param check
	 */
	protected void testCheck(final Check check)
	{
		check.setMessage("XYZ");
		assertEquals("XYZ", check.getMessage());

		check.setGroups(Object.class);
		assertNotNull(check.getGroups());
		assertEquals(1, check.getGroups().length);
		assertEquals(Object.class, check.getGroups()[0]);

		check.setGroups(null);
		assertTrue(check.getGroups() == null || check.getGroups().length == 0);
	}
}
