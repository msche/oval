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
 *******************************************************************************/
package net.sf.oval;

import net.sf.oval.context.OValContext;
import net.sf.oval.exception.OValException;

import java.io.Serializable;

/**
 * interface for classes that can exclude the checking of constraints
 * 
 * @author Sebastian Thomschke
 */
public interface CheckExclusion extends Serializable
{
	/**
	 * @return the profiles, may return null
	 */
	String[] getProfiles();

	/**
	 * @param validatedObject the object/bean to validate the value against, for static fields or methods this is the class
	 * @param valueToValidate the value to validate, may be null when validating pre conditions for static methods
	 * @param validator the calling validator
	 * @return <code>true</code> if this check exclusion is active and must be satisfied
	 */
	boolean isActive(final Object validatedObject, final Object valueToValidate, final Validator validator);

	/**
	 * This method implements the validation logic
	 * 
	 * @param check a check that OVal is about to validate
	 * @param validatedObject the object/bean to validate the value against, for static fields or methods this is 
	 * the class
	 * @param valueToValidate the value to validate, may be null when validating pre conditions for static methods
	 * @param context the validation context (e.g. a field, a constructor parameter or a method parameter)
	 * @param validator the calling validator
	 * @return true if the value satisfies the checked constraint
	 */
	boolean isCheckExcluded(Check check, Object validatedObject, Object valueToValidate, OValContext context,
			Validator validator) throws OValException;

	/**
	 * @param profiles the profiles to set
	 */
	void setProfiles(String... profiles);

}
