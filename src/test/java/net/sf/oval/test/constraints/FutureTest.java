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

import net.sf.oval.constraint.FutureCheck;

import java.text.DateFormat;
import java.util.Calendar;

/**
 * @author Sebastian Thomschke
 */
public class FutureTest extends AbstractContraintsTest
{
	public void testFuture()
	{
		final FutureCheck check = new FutureCheck();
		super.testCheck(check);
		assertTrue(check.isSatisfied(null, null, null, null));

		final Calendar cal = Calendar.getInstance();
		cal.add(Calendar.YEAR, 1);
		assertTrue(check.isSatisfied(null, cal, null, null));
		assertTrue(check.isSatisfied(null, cal.getTime(), null, null));
		assertTrue(check.isSatisfied(null, DateFormat.getDateTimeInstance().format(cal.getTime()), null, null));

		cal.add(Calendar.YEAR, -2);
		assertFalse(check.isSatisfied(null, cal, null, null));
		assertFalse(check.isSatisfied(null, cal.getTime(), null, null));
		assertFalse(check.isSatisfied(null, DateFormat.getDateTimeInstance().format(cal.getTime()), null, null));

		assertFalse(check.isSatisfied(null, "bla", null, null));
	}

	public void testTolerance()
	{
		final FutureCheck check = new FutureCheck();

		final Calendar cal = Calendar.getInstance();
		cal.add(Calendar.SECOND, -10);
		assertFalse(check.isSatisfied(null, cal, null, null));
		check.setTolerance(1500);
		assertFalse(check.isSatisfied(null, cal, null, null));
		check.setTolerance(15000);
		assertTrue(check.isSatisfied(null, cal, null, null));
	}
}
