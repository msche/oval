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
package net.sf.oval.constraint;

import net.sf.oval.ConstraintTarget;
import net.sf.oval.ConstraintViolation;
import net.sf.oval.configuration.annotation.Constraint;
import net.sf.oval.configuration.annotation.Constraints;
import net.sf.oval.constraint.CheckWithCheck.SimpleCheck;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Check the value by a method of the same class that takes the value as argument and returns true if valid
 * and false if invalid.
 * 
 * @author Sebastian Thomschke
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD, ElementType.TYPE})
@Constraint(validatedBy = CheckWithCheck.class)
public @interface CheckWith
{
	@Documented
	@Retention(RetentionPolicy.RUNTIME)
	@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD, ElementType.TYPE})
	@Constraints
	public @interface List
	{
		/**
		 * The CheckWith constraints.
		 */
		CheckWith[] value();

	}

	/**
	 * <p>In case the constraint is declared for an array, collection or map this controls how the constraint is applied to it and it's child objects.
	 * 
	 * <p><b>Default:</b> ConstraintTarget.CONTAINER
	 * 
	 * <p><b>Note:</b> This setting is ignored for object types other than array, map and collection.
	 */
	ConstraintTarget[] appliesTo() default ConstraintTarget.CONTAINER;

	/**
	 * this constraint will be ignored if the value to check is null
	 */
	boolean ignoreIfNull() default true;

	/**
	 * message to be used for the ContraintsViolatedException
	 * 
	 * @see ConstraintViolation
	 */
	String message() default "net.sf.oval.constraint.CheckWith.violated";

	/**
	 * Specifies the processing groups with which the constraint declaration is associated.
	 *
	 * Groups allow you to restrict the set of constraints applied during validation. Groups targeted are passed as parameters to the validate, validateProperty and validateValue methods. All constraints belonging to the targeted group are applied during the validation routine. If no group is passed, the Default group is assumed.
	 */
	Class<?>[] groups() default { };

	/**
	 * Check class to use for validation. If this class is an inner class
	 * it needs be declared as a <b>static</b> class. Otherwise
	 * check instantiation will fail.
	 * 
	 * @see net.sf.oval.constraint.CheckWithCheck.SimpleCheck
	 */
	Class< ? extends SimpleCheck> value();

}
