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

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Check if the specified regular expression pattern is not matched.
 * 
 * <br><br>
 * <b>Note:</b> This constraint is also satisfied when the value to validate is null, therefore you might also need to specified @NotNull
 * 
 * @author Sebastian Thomschke
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD})
@Constraint(validatedBy = NotMatchPatternCheck.class)
public @interface NotMatchPattern
{
	@Documented
	@Retention(RetentionPolicy.RUNTIME)
	@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD})
	@Constraints
	public @interface List
	{
		/**
		 * The NotMatchPattern constraints.
		 */
		NotMatchPattern[] value();

	}

	/**
	 * <p>In case the constraint is declared for an array, collection or map this controls how the constraint is applied to it and it's child objects.
	 * 
	 * <p><b>Default:</b> ConstraintTarget.VALUES
	 * 
	 * <p><b>Note:</b> This setting is ignored for object types other than array, map and collection.
	 */
	ConstraintTarget[] appliesTo() default ConstraintTarget.VALUES;

	/**
	 *  Match flags, a bit mask that may include
	 *         Pattern.CASE_INSENSITIVE, Pattern.MULTILINE, Pattern.DOTALL,
	 *         Pattern.UNICODE_CASE, Pattern.CANON_EQ
	 *
	 * @see java.util.regex.Pattern
	 */
	int[] flags() default 0;

	/**
	 * message to be used for the ContraintsViolatedException
	 * 
	 * @see ConstraintViolation
	 */
	String message() default "net.sf.oval.constraint.NotMatchPattern.violated";

	/**
	 * The regular expression(s) that must not match
	 * @see java.util.regex.Pattern
	 */
	String[] pattern();

	/**
	 * Specifies the processing groups with which the constraint declaration is associated.
	 *
	 * Groups allow you to restrict the set of constraints applied during validation. Groups targeted are passed as parameters to the validate, validateProperty and validateValue methods. All constraints belonging to the targeted group are applied during the validation routine. If no group is passed, the Default group is assumed.
	 */
	Class<?>[] groups() default { };

}
