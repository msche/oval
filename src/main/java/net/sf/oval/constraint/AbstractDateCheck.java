package net.sf.oval.constraint;

import net.sf.oval.Validator;
import net.sf.oval.configuration.annotation.AbstractAnnotationCheck;
import net.sf.oval.context.OValContext;
import net.sf.oval.exception.OValException;

import java.lang.annotation.Annotation;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Root class for date checks.
 */
public abstract class AbstractDateCheck<ConstraintAnnotation extends Annotation> extends AbstractAnnotationCheck<ConstraintAnnotation> {

    /**
     * Contains {@code DateFormat} instance that will be used to parse date string.
     */
    private SimpleDateFormat formatter = (SimpleDateFormat) DateFormat.getDateTimeInstance();

    private long tolerance;

    /**
     * @param format the format to set
     */
    public void setFormat(final String format) {
        if ((format != null) && (format.trim().length() > 0)) {
            setFormatter(new SimpleDateFormat(format));
        } else {
            setFormatter((SimpleDateFormat) DateFormat.getDateTimeInstance());
        }
        requireMessageVariablesRecreation();
    }

    /**
     * Returns date format. If no date format is specified the returned value will be null.
     *
     * @return Format to which the date strings must apply.
     */
    public String getFormat() {
        return formatter.toLocalizedPattern();
    }

    /**
     * @return the format
     */
    DateFormat getFormatter()
    {
        return formatter;
    }

    void setFormatter(SimpleDateFormat formatter) {
        net.sf.oval.internal.util.Assert.argumentNotNull("formatter", formatter);
        this.formatter = formatter;

    }

    /**
     * @return the tolerance
     */
    public long getTolerance()
    {
        return tolerance;
    }

    /**
     * @param tolerance the tolerance to set
     */
    public void setTolerance(final long tolerance)
    {
        this.tolerance = tolerance;
    }

    /**
     * This method implements the validation logic
     *
     * @param validatedObject the object/bean to validate the value against, for static fields or methods this is the class
     * @param valueToValidate the value to validate, may be null when validating pre conditions for static methods
     * @param context         the validation context (e.g. a field, a constructor parameter or a method parameter)
     * @param validator       the calling validator
     * @return true if the value satisfies the checked constraint
     */
    @Override
    public boolean isSatisfied(Object validatedObject, Object valueToValidate, OValContext context, Validator validator) throws OValException {
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
                    date = formatter.parse(stringValue);
                } catch (final ParseException ex) {
                    return false;
                }
            }
            return isSatisfied(validatedObject, date, context, validator);
        }
    }

    abstract boolean isSatisfied(Object validatedObject, Date valueToValidate, OValContext context, Validator validator) throws OValException;
}
