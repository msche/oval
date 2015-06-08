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
 *     Chris Pheby - inspectInterfaces
 *******************************************************************************/
package net.sf.oval.configuration.pojo.elements;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * @author Sebastian Thomschke
 */
public class ClassConfiguration
{
	private static final long serialVersionUID = -678113044888557518L;

	/**
	 * class at which this configuration applies.
	 */
	private final Class< ? > type;

	/**
	 * object level constraints configuration
	 */
	public ObjectConfiguration objectConfiguration;

	/**
	 * field constraints configuration
	 */
	private final Set<FieldChecks> fieldChecks = new LinkedHashSet();

	/**
	 * constructor constraints configuration
	 */
	private final Set<ConstructorConfiguration> constructorConfigurations = new LinkedHashSet<>();

	/**
	 * method constraints configuration
	 */
	private final Set<MethodConfiguration> methodConfigurations = new LinkedHashSet<>();

	/**
	 * Automatically apply field constraints to the corresponding parameters
	 * of constructors declared within the same class. A corresponding
	 * parameter is a parameter with the same name and type as the field.
	 */
	public boolean applyFieldConstraintsToConstructors;

	/**
	 * Automatically apply field constraints to the parameters of the
	 * corresponding setter methods declared within the same class. A
	 * corresponding setter method is a method following the JavaBean
	 * convention and its parameter has as the same type as the field.
	 */
	public boolean applyFieldConstraintsToSetters;

	/**
	 * Specifies whether annotations can be applied to interfaces that this class implements,
	 * supporting a documentation function
	 */
	public boolean inspectInterfaces;

	/**
	 * Constructor Class configuration
	 *
	 * @param type Type of class at which this configuration applies
	 */
	public ClassConfiguration(Class< ? > type) {
		this.type = type;
	}

	/**
	 * Returns type of class at which this configuration applies
	 */
	public Class<?> getType() {
		return type;
	}

    /**
     * Returns set of checks that need to be applied to fields within the class
     */
    public Set<FieldChecks> getFieldChecks() {
        return Collections.unmodifiableSet(fieldChecks);
    }

    /**
     * Append checks for fields within class
     *
     * @para fieldChecks checks that apply to certain field within class
     */
    public void addChecks(FieldChecks fieldChecks) {
        this.fieldChecks.add(fieldChecks);
    }

	/**
	 * Append checks for method within class
	 */
	public void addChecks(MethodConfiguration checks) {
		methodConfigurations.add(checks);
	}

	/**
	 * Returns a set of checks that need to be applied to the methods within this class
	 */
	public Set<MethodConfiguration> getMethodChecks() {
		return Collections.unmodifiableSet(methodConfigurations);
	}


	/**
	 * Append checks for constructors within class
	 */
	public void addChecks(ConstructorConfiguration checks) {
		this.constructorConfigurations.add(checks);
	}

	/**
	 * Returns a set of checks that need to be applied to the constructors within this class
	 */
	public Set<ConstructorConfiguration> getConstructorChecks() {
		return Collections.unmodifiableSet(constructorConfigurations);
	}
}
