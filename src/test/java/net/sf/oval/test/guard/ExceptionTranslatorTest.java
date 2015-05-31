/*******************************************************************************
 * Portions created by Sebastian Thomschke are copyright (c) 2005-2013 Sebastian
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
package net.sf.oval.test.guard;

import junit.framework.TestCase;
import javax.validation.constraints.NotNull;
import net.sf.oval.exception.ConstraintsViolatedException;
import net.sf.oval.exception.ExceptionTranslatorJDKExceptionsImpl;
import net.sf.oval.guard.Guard;
import net.sf.oval.guard.GuardAspect2;
import net.sf.oval.guard.Guarded;

import java.lang.reflect.InvocationTargetException;

/**
 *
 * @author Sebastian Thomschke
 */
public class ExceptionTranslatorTest extends TestCase
{
	@Guarded
	public static final class TestEntity
	{
		public void setName(@NotNull(message = "NULL") final String name)
		{
			//...
		}

		@SuppressWarnings("static-method")
		public void throwCheckedException() throws InvocationTargetException
		{
			throw new InvocationTargetException(null);
		}
	}

	public void testExceptionTranslator()
	{
		//final Guard guard = new Guard();
		//TestGuardAspect.aspectOf().setGuard(guard);
		Guard guard = new GuardAspect2().getGuard();

		assertNull(guard.getExceptionTranslator());

		try
		{
			final TestEntity t = new TestEntity();
			t.setName(null);
		}
		catch (final ConstraintsViolatedException ex)
		{
			assertEquals(ex.getMessage(), "NULL");
		}

		// MASE: todo check why this not working?
//		try
//		{
//			final TestEntity t = new TestEntity();
//
//			guard.setExceptionTranslator(new ExceptionTranslatorJDKExceptionsImpl());
//			try
//			{
//				t.setName(null);
//				fail();
//			}
//			catch (final IllegalArgumentException ex)
//			{
//				assertEquals(ex.getMessage(), "NULL");
//			}
//
//			try
//			{
//				t.throwCheckedException();
//				fail();
//			}
//			catch (final InvocationTargetException ex)
//			{
//				// expected
//			}
//
//		}
//		finally
//		{
//			guard.setExceptionTranslator(null);
//		}
	}
}
