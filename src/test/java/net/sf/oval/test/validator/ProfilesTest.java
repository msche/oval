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
package net.sf.oval.test.validator;

import junit.framework.TestCase;
import net.sf.oval.ConstraintViolation;
import net.sf.oval.Validator;
import javax.validation.constraints.NotNull;

import java.util.List;

/**
 * @author Sebastian Thomschke
 */
public class ProfilesTest extends TestCase
{
	public class Profile1 {}
	public class Profile2 {}
	public class Profile3 {}
	public class Profile4 {}

	protected static class Person
	{
		@NotNull(/* profiles = { "default" }, */message = "NOTNULL")
		public String city;

		@NotNull(groups = {Profile1.class}, message = "NOTNULL1")
		public String firstName;

		@NotNull(groups = {Profile2.class, Profile3.class}, message = "NOTNULL2")
		public String lastName;

		@NotNull(groups = {Profile3.class, Profile4.class}, message = "NOTNULL3")
		public String zipCode;
	}

	public void testAdhocProfiles()
	{
		final Validator validator = new Validator();

		// disable all profiles = no constraints by default
		validator.disableAllProfiles();
		final Person p = new Person();
		List<ConstraintViolation> violations = validator.validate(p, (String[]) null);
		assertEquals(0, violations.size());
		violations = validator.validate(p, Profile1.class);
		assertEquals(1, violations.size());
		assertEquals("NOTNULL1", violations.get(0).getMessage());
		violations = validator.validate(p, Profile1.class, Profile2.class);
		assertEquals(2, violations.size());

		// enable all profiles = all constraints by default
		validator.enableAllProfiles();
		violations = validator.validate(p, (String[]) null);
		assertEquals(4, violations.size());
		violations = validator.validate(p, Profile1.class);
		assertEquals(1, violations.size());
		assertEquals("NOTNULL1", violations.get(0).getMessage());
		violations = validator.validate(p, Profile1.class, Profile2.class);
		assertEquals(2, violations.size());
	}

	public void testProfilesGloballyDisabled()
	{
		final Validator validator = new Validator();

		// disable all profiles = no constraints
		validator.disableAllProfiles();
		assertFalse(validator.isProfileEnabled(Profile1.class.getName()));
		assertFalse(validator.isProfileEnabled(Profile2.class.getName()));
		assertFalse(validator.isProfileEnabled(Profile3.class.getName()));
		{
			final Person p = new Person();
			final List<ConstraintViolation> violations = validator.validate(p);
			assertEquals(0, violations.size());
		}

		// enable profile 1
		validator.enableProfile(Profile1.class);
		assertTrue(validator.isProfileEnabled(Profile1.class));
		{
			final Person p = new Person();
			final List<ConstraintViolation> violations = validator.validate(p);
			assertEquals(1, violations.size());
			assertEquals("NOTNULL1", violations.get(0).getMessage());
		}

		// enable profile 1 + 2
		validator.enableProfile(Profile2.class);
		assertTrue(validator.isProfileEnabled(Profile2.class));
		{
			final Person p = new Person();
			final List<ConstraintViolation> violations = validator.validate(p);
			assertEquals(2, violations.size());
		}

		// enable profile 1 + 2 + 3
		validator.enableProfile(Profile3.class);
		assertTrue(validator.isProfileEnabled(Profile3.class));
		{
			final Person p = new Person();
			final List<ConstraintViolation> violations = validator.validate(p);
			assertEquals(3, violations.size());
		}

		// enable profile 1 + 2 + 3 + 4
		assertFalse(validator.isProfileEnabled(Profile4.class));
		validator.enableProfile(Profile4.class);
		assertTrue(validator.isProfileEnabled(Profile4.class));
		{
			final Person p = new Person();
			final List<ConstraintViolation> violations = validator.validate(p);
			assertEquals(3, violations.size());
		}

		// enable profile 1 + 2 + 3 + 4 + default
		assertFalse(validator.isProfileEnabled("default"));
		validator.enableProfile("default");
		assertTrue(validator.isProfileEnabled("default"));
		{
			final Person p = new Person();
			final List<ConstraintViolation> violations = validator.validate(p);
			assertEquals(4, violations.size());
		}
	}

	public void testProfilesGloballyEnabled()
	{
		final Validator validator = new Validator();

		validator.enableAllProfiles();
		{
			final Person p = new Person();
			final List<ConstraintViolation> violations = validator.validate(p);
			assertEquals(4, violations.size());
		}

		assertTrue(validator.isProfileEnabled(Profile1.class));
		validator.disableProfile(Profile1.class);
		assertFalse(validator.isProfileEnabled(Profile1.class));
		{
			final Person p = new Person();
			final List<ConstraintViolation> violations = validator.validate(p);
			assertEquals(3, violations.size());
		}

		assertTrue(validator.isProfileEnabled(Profile2.class));
		validator.disableProfile(Profile2.class);
		assertFalse(validator.isProfileEnabled(Profile2.class));
		{
			final Person p = new Person();
			final List<ConstraintViolation> violations = validator.validate(p);
			assertEquals(3, violations.size());
		}

		assertTrue(validator.isProfileEnabled(Profile3.class));
		validator.disableProfile(Profile3.class);
		assertFalse(validator.isProfileEnabled(Profile3.class));
		{
			final Person p = new Person();
			final List<ConstraintViolation> violations = validator.validate(p);
			assertEquals(2, violations.size());
			if ("NOTNULL".equals(violations.get(0).getMessage()))
				assertEquals("NOTNULL3", violations.get(1).getMessage());
			else
			{
				assertEquals("NOTNULL3", violations.get(0).getMessage());
				assertEquals("NOTNULL", violations.get(1).getMessage());
			}
		}

		assertTrue(validator.isProfileEnabled(Profile4.class));
		validator.disableProfile(Profile4.class);
		assertFalse(validator.isProfileEnabled(Profile4.class));
		{
			final Person p = new Person();
			final List<ConstraintViolation> violations = validator.validate(p);
			assertEquals(1, violations.size());
		}

		assertTrue(validator.isProfileEnabled("default"));
		validator.disableProfile("default");
		assertFalse(validator.isProfileEnabled("default"));
		{
			final Person p = new Person();
			final List<ConstraintViolation> violations = validator.validate(p);
			assertEquals(0, violations.size());
		}
	}
}
