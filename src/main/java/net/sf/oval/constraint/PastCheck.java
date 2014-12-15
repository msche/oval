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

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;

import net.sf.oval.ConstraintTarget;
import net.sf.oval.Validator;
import net.sf.oval.configuration.annotation.AbstractAnnotationCheck;
import net.sf.oval.context.OValContext;

/**
 * Validates whether passed date is in the past.
 *
 * @author Sebastian Thomschke
 */
public class PastCheck extends AbstractAnnotationCheck<Past> {
    private static final long serialVersionUID = 1L;

    /**
     * Contains {@code DateFormat} instance that will be used to parse date string.
     */
    private static final DateFormat FORMATTER = DateFormat.getDateTimeInstance();

    private long tolerance;

    @Override
    public void configure(final Past constraintAnnotation) {
        super.configure(constraintAnnotation);
        setTolerance(constraintAnnotation.tolerance());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected ConstraintTarget[] getAppliesToDefault() {
        return new ConstraintTarget[]{ConstraintTarget.VALUES};
    }

    /**
     * @return the tolerance
     */
    public long getTolerance() {
        return tolerance;
    }

    /**
     * {@inheritDoc}
     */
    public boolean isSatisfied(final Object validatedObject, final Object valueToValidate, final OValContext context,
                               final Validator validator) {
        if (valueToValidate == null) {
            return true;
        } else {
            Date date;

            if (valueToValidate instanceof Date) {
                date = (Date) valueToValidate;
            } else if (valueToValidate instanceof Calendar) {
                date = ((Calendar) valueToValidate).getTime();
            } else {
                // see if we can extract a date based on the object's String representation
                final String stringValue = valueToValidate.toString();
                try {
                    date = FORMATTER.parse(stringValue);
                } catch (final ParseException ex) {
                    return false;
                }
            }
            final long now = System.currentTimeMillis() + tolerance;
            return date.getTime() < now;
        }
    }

    /**
     * @param tolerance the tolerance to set
     */
    public void setTolerance(final long tolerance) {
        this.tolerance = tolerance;
    }
}
