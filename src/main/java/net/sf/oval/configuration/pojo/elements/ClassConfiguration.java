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

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * @author Sebastian Thomschke
 */
public class ClassConfiguration extends ConfigurationElement
{
	private static final long serialVersionUID = 1L;

	/**
	 * class at which this configuration applies.
	 */
	public Class< ? > type;

	/**
	 * object level constraints configuration
	 */
	public ObjectConfiguration objectConfiguration;

	/**
	 * field constraints configuration
	 */
	private Set<FieldChecks> fieldChecks = new LinkedHashSet();

	/**
	 * constructor constraints configuration
	 */
	public Set<ConstructorConfiguration> constructorConfigurations;

	/**
	 * method constraints configuration
	 */
	public Set<MethodConfiguration> methodConfigurations;

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
	 * Declares if parameter values of constructors and methods are expected to be not null.
	 * This can be weakened by using the @net.sf.oval.constraint.exclusion.Nullable annotation on specific parameters.
	 */
	public boolean assertParametersNotNull;

	/**
	 * Specifies whether annotations can be applied to interfaces that this class implements,
	 * supporting a documentation function
	 */
	public boolean inspectInterfaces;

    /**
     * Returns set of checks that need to be applied to fields within the class
     */
    public Set<FieldChecks> getFieldChecks() {
        return fieldChecks;
    }

    /**
     * Append checks for fields within class
     *
     * @para fieldChecks checks that apply to certain field within class
     */
    public void addFieldChecks(FieldChecks fieldChecks) {
        this.fieldChecks.add(fieldChecks);
    }
}
