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

import net.sf.oval.constraint.DecimalMaxCheck;

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * @author Sebastian Thomschke
 */
public class DecimalMaxTest extends AbstractContraintsTest
{
	public void testMax()
	{
		final DecimalMaxCheck check = new DecimalMaxCheck();
		super.testCheck(check);
		assertTrue(check.isSatisfied(null, null, null, null));

		check.setMax(100);
		assertEquals(100.0, check.getMax());

		assertTrue(check.isSatisfied(null, "50", null, null));
		assertTrue(check.isSatisfied(null, 50, null, null));
		assertTrue(check.isSatisfied(null, (byte) 50, null, null));
		assertTrue(check.isSatisfied(null, (short) 50, null, null));
		assertTrue(check.isSatisfied(null, (float) 50.0, null, null));
		assertTrue(check.isSatisfied(null, 50.0, null, null));
		assertTrue(check.isSatisfied(null, BigDecimal.valueOf(50), null, null));
		assertTrue(check.isSatisfied(null, BigDecimal.valueOf(50.0), null, null));
		assertTrue(check.isSatisfied(null, BigInteger.valueOf(50), null, null));

		assertTrue(check.isSatisfied(null, "100", null, null));
		assertTrue(check.isSatisfied(null, 100, null, null));
		assertTrue(check.isSatisfied(null, (byte) 100, null, null));
		assertTrue(check.isSatisfied(null, (short) 100, null, null));
		assertTrue(check.isSatisfied(null, (float) 100.0, null, null));
		assertTrue(check.isSatisfied(null, 100.0, null, null));
		assertTrue(check.isSatisfied(null, BigDecimal.valueOf(100), null, null));
		assertTrue(check.isSatisfied(null, BigDecimal.valueOf(100.0), null, null));
		assertTrue(check.isSatisfied(null, BigInteger.valueOf(100), null, null));

		assertFalse(check.isSatisfied(null, "110", null, null));
		assertFalse(check.isSatisfied(null, 110, null, null));
		assertFalse(check.isSatisfied(null, (byte) 110, null, null));
		assertFalse(check.isSatisfied(null, (short) 110, null, null));
		assertFalse(check.isSatisfied(null, (float) 110.0, null, null));
		assertFalse(check.isSatisfied(null, 110.0, null, null));
		assertFalse(check.isSatisfied(null, BigDecimal.valueOf(110), null, null));
		assertFalse(check.isSatisfied(null, BigDecimal.valueOf(110.0), null, null));
		assertFalse(check.isSatisfied(null, BigInteger.valueOf(110), null, null));

		assertFalse(check.isSatisfied(null, "", null, null));
		assertFalse(check.isSatisfied(null, "sdfQ", null, null));
	}
}
