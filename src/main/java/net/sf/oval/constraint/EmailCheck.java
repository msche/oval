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
import net.sf.oval.configuration.annotation.AbstractAnnotationCheck;
import net.sf.oval.configuration.annotation.ConstraintAnnotationSettings;
import net.sf.oval.context.OValContext;
import net.sf.oval.exception.OValException;

import java.util.regex.Pattern;

/**
 *
 * Check if the value is a valid e-mail address. The check is performed based on a regular expression.
 *
 * @author Sebastian Thomschke
 */
public final class EmailCheck extends AbstractAnnotationCheck<Email> {
    private static final long serialVersionUID = 1886290528792753052L;

    private static final String SPECIAL_CHARACTERS = "'\\(\\)\\-\\.`";
    private static final String ASCII = "\\w " + SPECIAL_CHARACTERS;
    private static final String ASCII_WITHOUT_COMMA = "[" + ASCII + "]+";
    private static final String ASCII_WITH_COMMA = "\"[" + ASCII + ",]+\"";
    private static final String ASCII_WITH_QUESTION_MARK_AND_EQUALS = "[" + ASCII + "\\?\\=]+";
    private static final String MIME_ENCODED = "\\=\\?" + ASCII_WITH_QUESTION_MARK_AND_EQUALS + "\\?\\=";
    private static final String NAME = "(" + ASCII_WITHOUT_COMMA + "|" + ASCII_WITH_COMMA + "|" + MIME_ENCODED + ")";

    private static final String EMAIL_BASE_PATTERN = "['_A-Za-z0-9-&]+(\\.['_A-Za-z0-9-&]+)*[.]{0,1}@([A-Za-z0-9-])+(\\.[A-Za-z0-9-]+)*((\\.[A-Za-z0-9]{2,})|(\\.[A-Za-z0-9]{2,}\\.[A-Za-z0-9]{2,}))";

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^" + EMAIL_BASE_PATTERN + "$");

    private static final Pattern EMAIL_WITH_PERSONAL_NAME_PATTERN = Pattern.compile("^(" + EMAIL_BASE_PATTERN + "|"
            + NAME + " +<" + EMAIL_BASE_PATTERN + ">)$");

    /**
     * Indicates whether the e-mail address can contain a personal name as defined in RFC822,
     * e.g. "Personal Name <user@host.domain>".
     */
    private boolean allowPersonalName;

    /**
     * {@inheritDoc}
     */
    @Override
    public void configure(final Email constraintAnnotation) {
        //super.configure(constraintAnnotation);
        configure(getSettings(constraintAnnotation));
        setAllowPersonalName(constraintAnnotation.allowPersonalName());
    }

    /**
     * Returns value object {@code ConstraintAnnotationSettings} containing the basic settings of the constraint annotations
     *
     * @param constraintAnnotation Annotation from which the settings will be extracted
     *
     * @return Value object {@code ConstraintAnnotationSettings}.
     */
    protected ConstraintAnnotationSettings getSettings(final  Email constraintAnnotation) {

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
     * @return the allowPersonalName
     */
    public boolean isAllowPersonalName() {
        return allowPersonalName;
    }

    /**
     * This method implements the validation logic
     *
     * Note: This constraint is also satisfied when the value to validate is null
     *
     * @param validatedObject the object/bean to validate the value against, for static fields or methods this is the class
     * @param valueToValidate the value to validate, may be null when validating pre conditions for static methods
     * @param context the validation context (e.g. a field, a constructor parameter or a method parameter)
     * @param validator the calling validator
     *
     * @return true if the value satisfies the checked constraint
     */
    public boolean isSatisfied(final Object validatedObject, final Object valueToValidate, final OValContext context,
                               final Validator validator) throws OValException {
        if (valueToValidate == null) {
            return true;
        } else {
            final String stringToValidate = valueToValidate.toString();
            if (allowPersonalName) {
                return EMAIL_WITH_PERSONAL_NAME_PATTERN.matcher(stringToValidate).matches();
            } else {
                return EMAIL_PATTERN.matcher(stringToValidate).matches();
            }
        }
    }

    /**
     * @param allowPersonalName the allowPersonalName to set
     */
    public void setAllowPersonalName(final boolean allowPersonalName) {
        this.allowPersonalName = allowPersonalName;
    }
}
