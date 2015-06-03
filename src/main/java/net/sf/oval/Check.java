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
package net.sf.oval;

import net.sf.oval.context.OValContext;
import net.sf.oval.exception.OValException;

import java.io.Serializable;
import java.util.Map;

/**
 * interface for classes that can check/validate if a constraint is satisfied
 *
 * @author Sebastian Thomschke
 */
public interface Check extends Serializable
{
	/**
	 * <p>In case the constraint is declared for an array, collection or map this controls how the constraint is applied to it and it's child objects.
	 *
	 * <p><b>Default:</b> ConstraintTarget.CONTAINER
	 *
	 * <p><b>Note:</b> This setting is ignored for object types other than array, map and collection.
	 */
	ConstraintTarget[] getAppliesTo();

	/**
	 * @return Returns the context where the constraint was declared.
	 *
	 * @see net.sf.oval.context.ClassContext
	 * @see net.sf.oval.context.FieldContext
	 * @see net.sf.oval.context.MethodEntryContext
	 * @see net.sf.oval.context.MethodExitContext
	 * @see net.sf.oval.context.MethodParameterContext
	 * @see net.sf.oval.context.MethodReturnValueContext
	 */
	OValContext getContext();

	/**
	 * gets the default message that is displayed if a corresponding message key
	 * is not found in the messages properties file
	 * <br>
	 * default processed place holders are:
	 * <ul>
	 * <li>{context} => specifies which getter, method parameter or field was validated
	 * <li>{invalidValue} => string representation of the validated value
	 * </ul>
	 */
	String getMessage();

	/**
	 * values that are used to fill place holders when rendering the error message.
	 * A key "min" with a value "4" will replace the place holder {min} in an error message
	 * like "Value cannot be smaller than {min}" with the string "4".
	 */
	Map<String, ? extends Serializable> getMessageVariables();

	/**
	 * @return the profiles, may return null
	 */
	String[] getGroups();

	/**
	 *
	 * @param validatedObject the object/bean to validate the value against, for static fields or methods this is the class
	 * @param valueToValidate the value to validate, may be null when validating pre conditions for static methods
	 * @param validator the calling validator
	 * @return <code>true</code> if this check is active and must be satisfied
	 */
	boolean isActive(Object validatedObject, Object valueToValidate, Validator validator);

	/**
	 * This method implements the validation logic
	 *
	 * @param validatedObject the object/bean to validate the value against, for static fields or methods this is the class
	 * @param valueToValidate the value to validate, may be null when validating pre conditions for static methods
	 * @param context the validation context (e.g. a field, a constructor parameter or a method parameter)
	 * @param validator the calling validator
	 * @return true if the value satisfies the checked constraint
	 */
	boolean isSatisfied(Object validatedObject, Object valueToValidate, OValContext context, Validator validator)
			throws OValException;

	/**
	 * @param target the constraint target to set
	 */
	void setAppliesTo(ConstraintTarget... target);

	/**
	 * @param context the context to set
	 */
	void setContext(OValContext context);

	/**
	 * sets the default message that is displayed if a corresponding message key
	 * is not found in the messages properties file
	 *
	 * <br>
	 * default processed place holders are:
	 * <ul>
	 * <li>{context} => specifies which getter, method parameter or field was validated
	 * <li>{invalidValue} => string representation of the validated value
	 * </ul>
	 */
	void setMessage(String message);

	/**
	 * @param profiles the profiles to set
	 */
	void setGroups(Class... profiles);

}
