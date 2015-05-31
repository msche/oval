/*******************************************************************************
 * Portions created by Sebastian Thomschke are copyright (c) 2005-2014 Sebastian
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
import net.sf.oval.ConstraintViolation;
import net.sf.oval.constraint.Length;
import javax.validation.constraints.NotNull;
import net.sf.oval.context.ConstructorParameterContext;
import net.sf.oval.context.FieldContext;
import net.sf.oval.exception.ConstraintsViolatedException;
import net.sf.oval.exception.OValException;
import net.sf.oval.guard.ConstraintsViolatedAdapter;
import net.sf.oval.guard.Guard;
import net.sf.oval.guard.GuardAspect2;
import net.sf.oval.guard.Guarded;

import java.util.List;

/**
 * @author Sebastian Thomschke
 */
public class ParameterConstraintsTest extends TestCase
{
	@Guarded
	public static class TestEntity
	{
		@NotNull(message = "NOT_NULL")
		private String name = "";

		/**
		 * Constructor 1
		 *
		 * @param name
		 */
		public TestEntity(@NotNull(message = "NOT_NULL") final String name)
		{
			this.name = name;
		}

		/**
		 * Constructor 2
		 *
		 * @param name
		 * @param bla
		 */
		public TestEntity(final String name, final int bla)
		{
			this.name = name;
		}

		public void setName(@NotNull(message = "NOT_NULL") @Length(max = 4, message = "LENGTH") final String name)
		{
			this.name = name;
		}
	}

	@SuppressWarnings("unused")
	public void testConstructorParameterConstraints()
	{
		//final Guard guard = new Guard();
		//TestGuardAspect.aspectOf().setGuard(guard);
		Guard guard = new GuardAspect2().getGuard();

		/*
		 * Testing Constructor 1
		 * parameter constraint
		 */
		try
		{
			new TestEntity(null);
			fail();
		}
		catch (final ConstraintsViolatedException e)
		{
			final ConstraintViolation[] violations = e.getConstraintViolations();
			assertNotNull(violations);
			assertEquals(1, violations.length);
			assertTrue(violations[0].getMessage().equals("NOT_NULL"));
			assertTrue(violations[0].getContext() instanceof ConstructorParameterContext);
		}

		new TestEntity("test");

		/*
		 * Testing Constructor 2
		 * invariant constraint
		 */
		try
		{
			new TestEntity(null, 100);
			fail();
		}
		catch (final ConstraintsViolatedException e)
		{
			final ConstraintViolation[] violations = e.getConstraintViolations();
			assertNotNull(violations);
			assertEquals(1, violations.length);
			assertTrue(violations[0].getMessage().equals("NOT_NULL"));
			assertTrue(violations[0].getContext() instanceof FieldContext);
		}
	}

	public void testMethodParameters()
	{
		//final Guard guard = new Guard();
		//TestGuardAspect.aspectOf().setGuard(guard);
		Guard guard = new GuardAspect2().getGuard();

		try
		{
			final TestEntity t1 = new TestEntity("");
			t1.setName(null);
			fail();
		}
		catch (final ConstraintsViolatedException e)
		{
			final ConstraintViolation[] violations = e.getConstraintViolations();
			assertNotNull(violations);
			assertTrue(violations.length > 0);
			assertTrue(violations[0].getMessage().equals("NOT_NULL"));
		}

		try
		{
			final TestEntity t1 = new TestEntity("");
			t1.setName("12345678");
			fail();
		}
		catch (final ConstraintsViolatedException e)
		{
			final ConstraintViolation[] violations = e.getConstraintViolations();
			assertNotNull(violations);
			assertTrue(violations.length > 0);
			assertTrue(violations[0].getMessage().equals("LENGTH"));
		}
	}

	public void testMethodParametersInProbeMode()
	{
//		//final Guard guard = new Guard();
//		//TestGuardAspect.aspectOf().setGuard(guard);
//		Guard guard = new GuardAspect2().getGuard();
//
//		final TestEntity entity = new TestEntity("");
//
//		//guard.enableProbeMode(entity);
//
//		final ConstraintsViolatedAdapter va = new ConstraintsViolatedAdapter();
//		guard.addListener(va, entity);
//
//		try {
//			entity.setName(null);
//			//entity.setName("12345678");
//		} catch(OValException error) {
//			final List<ConstraintViolation> violations = va.getConstraintViolations();
//			assertTrue(violations.size() == 1);
//			assertTrue(violations.get(0).getMessage().equals("NOT_NULL"));
//			//assertTrue(violations.get(1).getMessage().equals("LENGTH"));
//			violations.clear();
//		}
//
//		try {
//			//entity.setName(null);
//			entity.setName("12345678");
//		} catch(OValException error) {
//			final List<ConstraintViolation> violations = va.getConstraintViolations();
//			assertTrue(violations.size() == 1);
//			assertTrue(violations.get(0).getMessage().equals("LENGTH"));
//			violations.clear();
//		}
//
//		guard.removeListener(va, entity);
	}
}
