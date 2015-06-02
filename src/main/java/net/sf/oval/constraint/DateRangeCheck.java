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

import net.sf.oval.context.OValContext;
import net.sf.oval.exception.InvalidConfigurationException;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author Sebastian Thomschke
 */
public final class DateRangeCheck extends AbstractDateCheck<DateRange> {

    private static final long serialVersionUID = 1L;

    private String max;
    private String min;

    private transient Long maxMillis;
    private transient Long minMillis;

    /**
     * {@inheritDoc}
     */
    @Override
    public void configure(final DateRange constraintAnnotation) {
        setMessage(constraintAnnotation.message());
        setProfiles(constraintAnnotation.profiles());
        setMin(constraintAnnotation.min());
        setMax(constraintAnnotation.max());
        setFormat(constraintAnnotation.format());
        setTolerance(constraintAnnotation.tolerance());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Map<String, String> createMessageVariables() {
        final Map<String, String> messageVariables = new LinkedHashMap<>(3);
        messageVariables.put("min", min == null ? ".." : min);
        messageVariables.put("max", max == null ? ".." : max);
        messageVariables.put("format", getFormat());
        return messageVariables;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected ConstraintTarget[] getAppliesToDefault() {
        return new ConstraintTarget[]{ConstraintTarget.VALUES};
    }

    /**
     * @return the max
     */
    public String getMax() {
        return max;
    }

    private long getMaxMillis() throws InvalidConfigurationException {
        if (maxMillis == null) {
            if (max == null || max.length() == 0) {
                return Long.MAX_VALUE;
            } else if ("now".equals(max)) {
                return System.currentTimeMillis() + getTolerance();
            } else {
                final Calendar cal = Calendar.getInstance();
                cal.set(Calendar.HOUR_OF_DAY, 23);
                cal.set(Calendar.MINUTE, 59);
                cal.set(Calendar.SECOND, 59);
                cal.set(Calendar.MILLISECOND, 999);

                switch (max) {
                    case "today":
                        return cal.getTimeInMillis() + getTolerance();

                    case "tomorrow":
                        cal.add(Calendar.DAY_OF_YEAR, 1);
                        return cal.getTimeInMillis() + getTolerance();
                    case "yesterday":
                        cal.add(Calendar.DAY_OF_YEAR, -1);
                        return cal.getTimeInMillis() + getTolerance();
                    default:
                        try {
                            maxMillis = getFormatter().parse(max).getTime() + getTolerance();
                        } catch (final ParseException e) {
                            throw new InvalidConfigurationException("Unable to parse the max Date String", e);
                        }
                }
            }
        }
        return maxMillis;
    }

    /**
     * @return the min
     */
    public String getMin() {
        return min;
    }

    private long getMinMillis() throws InvalidConfigurationException {
        if (minMillis == null) {
            if (min == null || min.length() == 0) {
                return 0L;
            } else if ("now".equals(min)) {
                return System.currentTimeMillis() - getTolerance();
            }else {
                final Calendar cal = Calendar.getInstance();
                cal.set(Calendar.HOUR_OF_DAY, 0);
                cal.set(Calendar.MINUTE, 0);
                cal.set(Calendar.SECOND, 0);
                cal.set(Calendar.MILLISECOND, 0);

                switch(min) {

                    case "today":
                        return cal.getTimeInMillis() - getTolerance();

                    case "tomorrow":
                        cal.add(Calendar.DAY_OF_YEAR, 1);
                        return cal.getTimeInMillis() - getTolerance();

                    case "yesterday":
                        cal.add(Calendar.DAY_OF_YEAR, -1);
                        return cal.getTimeInMillis() - getTolerance();

                    default:
                        try {
                            minMillis = getFormatter().parse(min).getTime() - getTolerance();
                        } catch (final ParseException e) {
                            throw new InvalidConfigurationException("Unable to parse the min Date String", e);
                        }
                }
            }
        }
        return minMillis;
    }

    boolean isSatisfied(final Object validatedObject, Date valueToValidate, final OValContext context,
                        final Validator validator) {
        long valueInMillis = valueToValidate.getTime();
        return valueInMillis >= getMinMillis() && valueInMillis <= getMaxMillis();
    }

    /**
     * @param max the max to set
     */
    public void setMax(final String max) {
        this.max = max;
        maxMillis = null;
        requireMessageVariablesRecreation();
    }

    /**
     * @param min the min to set
     */
    public void setMin(final String min) {
        this.min = min;
        minMillis = null;
        requireMessageVariablesRecreation();
    }

    /**
     * @param tolerance the tolerance to set
     */
    public void setTolerance(final long tolerance) {
        super.setTolerance(tolerance);
        minMillis = null;
        maxMillis = null;
    }
}
