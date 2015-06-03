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
 * Check if the date is within the a date range.
 * 
 * <br><br>
 * <b>Note:</b> This constraint is also satisfied when the value to validate is null, therefore you might also need to specified @NotNull
 * 
 * @author Sebastian Thomschke
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD})
@Constraint(validatedBy = DateRangeCheck.class)
public @interface DateRange
{
	@Documented
	@Retention(RetentionPolicy.RUNTIME)
	@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD})
	@Constraints
	public @interface List
	{
		/**
		 * The DateRange constraints.
		 */
		DateRange[] value();

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
	 * The format of the specified dates in a form understandable by the SimpleDateFormat class.
	 * Defaults to the default format style of the default locale.
	 */
	String format() default "";

	/**
	 * The upper date compared against in the format specified with the dateFormat parameter. 
	 * If not specified then no upper boundary check is performed.<br>
	 * Special values are:
	 * <ul>
	 * <li><code>now</code>
	 * <li><code>today</code>
	 * <li><code>yesterday</code>
	 * <li><code>tomorrow</code>
	 * </ul>
	 */
	String max() default "";

	/**
	 * message to be used for the ContraintsViolatedException
	 * 
	 * @see ConstraintViolation
	 */
	String message() default "net.sf.oval.constraint.DateRange.violated";

	/**
	 * The lower date compared against in the format specified with the dateFormat parameter. 
	 * If not specified then no upper boundary check is performed.<br>
	 * Special values are:
	 * <ul>
	 * <li><code>now</code>
	 * <li><code>today</code>
	 * <li><code>yesterday</code>
	 * <li><code>tomorrow</code>
	 * </ul>
	 */
	String min() default "";

	/**
	 * Specifies the processing groups with which the constraint declaration is associated.
	 *
	 * Groups allow you to restrict the set of constraints applied during validation. Groups targeted are passed as parameters to the validate, validateProperty and validateValue methods. All constraints belonging to the targeted group are applied during the validation routine. If no group is passed, the Default group is assumed.
	 */
	Class<?>[] groups() default { };

	/**
	 * Tolerance in milliseconds the validated value can be beyond the min/max limits. 
	 * This is useful to compensate time differences in distributed environments where the clocks are not 100% in sync.
	 */
	int tolerance() default 0;
}
