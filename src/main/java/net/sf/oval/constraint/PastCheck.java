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
import net.sf.oval.Validator;
import net.sf.oval.configuration.annotation.ConstraintAnnotationSettings;
import net.sf.oval.context.OValContext;

import java.util.Date;

/**
 * Validates whether passed date is in the past.
 *
 * @author Sebastian Thomschke
 */
public final class PastCheck extends AbstractDateCheck<Past> {
    private static final long serialVersionUID = 1L;

    @Override
    public void configure(final Past constraintAnnotation) {
        super.configure(constraintAnnotation);
        setTolerance(constraintAnnotation.tolerance());
    }

    /**
     * Returns value object {@code ConstraintAnnotationSettings} containing the basic settings of the constraint annotation.
     *
     * @param constraintAnnotation Annotation from which the settings will be extracted
     *
     * @return Value object {@code ConstraintAnnotationSettings}.
     */
    protected ConstraintAnnotationSettings getSettings(final  Past constraintAnnotation) {

        ConstraintAnnotationSettings settings = new ConstraintAnnotationSettings.Builder()
                .message(constraintAnnotation.message())
                .appliesTo(constraintAnnotation.appliesTo())
                .errorCode(constraintAnnotation.errorCode())
                .severity(constraintAnnotation.severity())
                .profiles(constraintAnnotation.profiles())
                .target(constraintAnnotation.target())
                .when(constraintAnnotation.when())
                .build();
        return settings;
    }
    /**
     * {@inheritDoc}
     */
    @Override
    protected ConstraintTarget[] getAppliesToDefault() {
        return new ConstraintTarget[]{ConstraintTarget.VALUES};
    }

    /**
     * {@inheritDoc}
     */
    boolean isSatisfied(final Object validatedObject, final Date valueToValidate, final OValContext context,
                               final Validator validator) {
            final long now = System.currentTimeMillis() + getTolerance();
        return valueToValidate.getTime() < now;
    }

}
