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
import net.sf.oval.configuration.annotation.Constraint;
import net.sf.oval.configuration.annotation.Constraints;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Check if the value satisfies the all constraints of specified constraint set.
 *
 * @author Sebastian Thomschke
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD})
@Constraint(validatedBy = AssertConstraintSetCheck.class)
public @interface AssertConstraintSet {
    @Documented
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD})
    @Constraints
    public @interface List {
        /**
         * The AssertConstraintSet constraints.
         */
        AssertConstraintSet[] value();
    }

    /**
     * <p>In case the constraint is declared for an array, collection or map this controls how the constraint is applied to it and it's child objects.
     * <p/>
     * <p><b>Default:</b> ConstraintTarget.CONTAINER
     * <p/>
     * <p><b>Note:</b> This setting is ignored for object types other than array, map and collection.
     */
    ConstraintTarget[] appliesTo() default ConstraintTarget.CONTAINER;

    /**
     * The id of the constraint set to apply here<br>
     */
    String id();

    /**
     * Specifies the processing groups with which the constraint declaration is associated.
     * <p/>
     * Groups allow you to restrict the set of constraints applied during validation. Groups targeted are passed as parameters to the validate, validateProperty and validateValue methods. All constraints belonging to the targeted group are applied during the validation routine. If no group is passed, the Default group is assumed.
     */
    Class<?>[] groups() default {};

}
