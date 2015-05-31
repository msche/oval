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
import net.sf.oval.ConstraintTarget;
import net.sf.oval.ConstraintViolation;
import net.sf.oval.Validator;
import net.sf.oval.constraint.Length;
import net.sf.oval.constraint.NotEmpty;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import net.sf.oval.context.FieldContext;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Sebastian Thomschke
 */
public class ValidTest extends TestCase
{
	protected static class Address
	{
		@NotNull(message = "NOT_NULL_STREET")
		public String street;

		@NotNull
		public String city;

		@NotNull
		@Length(max = 6)
		@NotEmpty
		@Pattern(regexp = "^[0-9]*$")
		public String zipCode;

		@Valid
		public Person contact;
	}

	protected static class Person
	{
		@NotNull
		public String firstName;

		@NotNull
		public String lastName;

		@Valid
		public Address homeAddress;

		@Valid
		public List<Address> otherAddresses1;

		@Valid
		public Set<Address> otherAddresses2;

		@Valid
		public Set<Address> otherAddresses3;

	}

	protected static class Registry
	{
		@Valid
		public List<Address[]> addressClusters;

		@Valid
		public Map<String, List<Person>> personsByCity;

		@Valid
		public Map<String, Map<String, Address[]>> addressesByCityAndStreet;
	}

	public void testValidateFieldValue() throws SecurityException, NoSuchFieldException
	{
		final Validator validator = new Validator();

		final Person p = new Person();
		final Field fieldHomeAddress = p.getClass().getDeclaredField("homeAddress");
		final Address a = new Address();

		final List<ConstraintViolation> violations = validator.validateFieldValue(p, fieldHomeAddress, a);
		assertEquals(1, violations.size());
		//assertEquals("ASSERT_VALID", violations.get(0).getMessage());
		assertEquals(a, violations.get(0).getInvalidValue());
		assertEquals(3, violations.get(0).getCauses().length);
		for (final ConstraintViolation cv : violations.get(0).getCauses())
			if ("NOT_NULL_STREET".equals(cv.getMessage())) assertEquals("street", ((FieldContext) cv.getContext()).getField().getName());
		a.street = "The Street";
		a.city = "The City";
		a.zipCode = "12345";
		assertEquals(0, validator.validateFieldValue(p, fieldHomeAddress, a).size());
	}

	public void testCollectionValues()
	{
		final Validator validator = new Validator();

		final Person p = new Person();
		p.firstName = "John";
		p.lastName = "Doe";
		p.otherAddresses1 = new ArrayList<Address>();
		p.otherAddresses2 = new HashSet<Address>();
		p.otherAddresses3 = new HashSet<Address>();

		final Address a = new Address();
		a.street = "The Street";
		a.city = "The City";
		a.zipCode = null;
		assertEquals(1, validator.validate(a).size());

		p.otherAddresses1.add(a);
		assertEquals(1, validator.validate(p).size());

		p.otherAddresses1.remove(a);
		p.otherAddresses2.add(a);
		assertEquals(1, validator.validate(p).size());

		p.otherAddresses3.add(a);
		assertEquals(2, validator.validate(p).size());
	}

	public void testRecursion()
	{
		final Validator validator = new Validator();

		final Registry registry = new Registry();

		// nulled collections and maps are valid
		assertTrue(validator.validate(registry).size() == 0);

		registry.addressesByCityAndStreet = new HashMap<String, Map<String, Address[]>>();
		registry.addressClusters = new ArrayList<Address[]>();
		registry.personsByCity = new HashMap<String, List<Person>>();

		// empty collections and maps are valid
		assertEquals(0, validator.validate(registry).size());

		final Person invalidPerson1 = new Person();
		final Person invalidPerson2 = new Person();

		// map with an empty list is valid
		registry.personsByCity.put("city1", new ArrayList<Person>());
		assertEquals(0, validator.validate(registry).size());

		registry.personsByCity.put("city1", Arrays.asList(new Person[]{invalidPerson1}));
		assertEquals(1, validator.validate(registry).size());

		registry.personsByCity.put("city2", Arrays.asList(new Person[]{invalidPerson2}));
		assertEquals(2, validator.validate(registry).size());

		registry.personsByCity.clear();
		registry.personsByCity.put("city1", Arrays.asList(new Person[]{invalidPerson1, invalidPerson1, invalidPerson2, invalidPerson2}));
		assertEquals(4, validator.validate(registry).size());

		registry.personsByCity.clear();

		// list with an array with empty elements is valid
		registry.addressClusters.add(new Address[10]);
		assertEquals(0, validator.validate(registry).size());

		final Address invalidAddress1 = new Address();
		final Address invalidAddress2 = new Address();

		registry.addressClusters.add(new Address[10]);
		assertEquals(0, validator.validate(registry).size());

		registry.addressClusters.add(new Address[]{invalidAddress1, invalidAddress2, invalidAddress1, invalidAddress2});
		assertEquals(4, validator.validate(registry).size());

		registry.addressClusters.clear();

		// map with an entry with an empty map is valid
		registry.addressesByCityAndStreet.put("city1", new HashMap<String, Address[]>());
		assertEquals(0, validator.validate(registry).size());

		// map with an entry with an map with an element with an empty array is valid
		registry.addressesByCityAndStreet.get("city1").put("street1", new Address[0]);
		assertEquals(0, validator.validate(registry).size());

		registry.addressesByCityAndStreet.get("city1").put("street1",
				new Address[]{invalidAddress1, invalidAddress1, invalidAddress2, invalidAddress2});
		assertEquals(4, validator.validate(registry).size());
	}

	public void testScalarValues()
	{
		final Validator validator = new Validator();

		final Person p = new Person();
		p.firstName = "John";
		p.lastName = "Doe";
		assertEquals(0, validator.validate(p).size());

		final Address a = new Address();
		a.street = "The Street";
		a.city = "The City";
		a.zipCode = "12345";
		assertEquals(0, validator.validate(a).size());

		// make the address invalid
		a.zipCode = null;
		assertEquals(1, validator.validate(a).size());

		// associate the invalid address with the person check the person for validity
		p.homeAddress = a;
		List<ConstraintViolation> violations = validator.validate(p);
		assertEquals(1, violations.size());
		//assertEquals("ASSERT_VALID", violations.get(0).getMessage());

		// test circular dependencies
		a.contact = p;
		violations = validator.validate(p);
		assertEquals(1, violations.size());
		//assertEquals("ASSERT_VALID", violations.get(0).getMessage());
	}
}
