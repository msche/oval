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
		validator.disableAllGroups();
		final Person p = new Person();
		List<ConstraintViolation> violations = validator.validate(p, (Class[]) null);
		assertEquals(1, violations.size());
		violations = validator.validate(p, Profile1.class);
		assertEquals(2, violations.size());
		violations = validator.validate(p, Profile1.class, Profile2.class);
		assertEquals(3, violations.size());

		// enable all profiles = all constraints by default
		validator.enableGroups(Profile1.class, Profile2.class, Profile3.class, Profile4.class);
		violations = validator.validate(p, (Class[]) null);
		assertEquals(4, violations.size());
		violations = validator.validate(p, Profile1.class);
		assertEquals(2, violations.size());
		violations = validator.validate(p, Profile1.class, Profile2.class);
		assertEquals(3, violations.size());
	}

	public void testProfilesGloballyDisabled()
	{
		final Validator validator = new Validator();

		// disable all profiles = no constraints
		validator.disableAllGroups();
		assertFalse(validator.isGroupEnabled(Profile1.class));
		assertFalse(validator.isGroupEnabled(Profile2.class));
		assertFalse(validator.isGroupEnabled(Profile3.class));
        assertTrue(validator.isGroupEnabled(null));
		{
			final Person p = new Person();
			final List<ConstraintViolation> violations = validator.validate(p);
			assertEquals(1, violations.size());
		}

		// enable profile 1 + default
		validator.enableGroup(Profile1.class);
		assertTrue(validator.isGroupEnabled(Profile1.class));
		{
			final Person p = new Person();
			final List<ConstraintViolation> violations = validator.validate(p);
			assertEquals(2, violations.size());
		}

		// enable profile 1 + 2 + default
		validator.enableGroup(Profile2.class);
		assertTrue(validator.isGroupEnabled(Profile2.class));
		{
			final Person p = new Person();
			final List<ConstraintViolation> violations = validator.validate(p);
			assertEquals(3, violations.size());
		}

		// enable profile 1 + 2 + 3 + default
		validator.enableGroup(Profile3.class);
		assertTrue(validator.isGroupEnabled(Profile3.class));
		{
			final Person p = new Person();
			final List<ConstraintViolation> violations = validator.validate(p);
			assertEquals(4, violations.size());
		}

		// enable profile 1 + 2 + 3 + 4 + default
		assertFalse(validator.isGroupEnabled(Profile4.class));
		validator.enableGroup(Profile4.class);
		assertTrue(validator.isGroupEnabled(Profile4.class));
		{
			final Person p = new Person();
			final List<ConstraintViolation> violations = validator.validate(p);
			assertEquals(4, violations.size());
		}

		// enable profile 1 + 2 + 3 + 4 + default
		assertTrue(validator.isGroupEnabled(null));
		{
			final Person p = new Person();
			final List<ConstraintViolation> violations = validator.validate(p);
			assertEquals(4, violations.size());
		}
	}

	public void testProfilesGloballyEnabled()
	{
		final Validator validator = new Validator();

		validator.enableGroups(Profile1.class, Profile2.class, Profile3.class, Profile4.class);
		{
			final Person p = new Person();
			final List<ConstraintViolation> violations = validator.validate(p);
			assertEquals(4, violations.size());
		}

		assertTrue(validator.isGroupEnabled(Profile1.class));
		validator.disableGroup(Profile1.class);
		assertFalse(validator.isGroupEnabled(Profile1.class));
		{
			final Person p = new Person();
			final List<ConstraintViolation> violations = validator.validate(p);
			assertEquals(3, violations.size());
		}

		assertTrue(validator.isGroupEnabled(Profile2.class));
		validator.disableGroup(Profile2.class);
		assertFalse(validator.isGroupEnabled(Profile2.class));
		{
			final Person p = new Person();
			final List<ConstraintViolation> violations = validator.validate(p);
			assertEquals(3, violations.size());
		}

		assertTrue(validator.isGroupEnabled(Profile3.class));
		validator.disableGroup(Profile3.class);
		assertFalse(validator.isGroupEnabled(Profile3.class));
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

		assertTrue(validator.isGroupEnabled(Profile4.class));
		validator.disableGroup(Profile4.class);
		assertFalse(validator.isGroupEnabled(Profile4.class));
		{
			final Person p = new Person();
			final List<ConstraintViolation> violations = validator.validate(p);
			assertEquals(1, violations.size());
		}

//		assertTrue(validator.isProfileEnabled("default"));isGroupEnabledisablePdisableGroupt");
//		assertFalse(validator.isProfileEnabled("default"));isGroupEnabledl Person p = new Person();
//			final List<ConstraintViolation> violations = validator.validate(p);
//			assertEquals(0, violations.size());
//		}
	}
}
