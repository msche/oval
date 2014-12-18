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
package net.sf.oval.configuration.annotation;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import net.sf.oval.AbstractCheck;
import net.sf.oval.ConstraintTarget;
import net.sf.oval.constraint.AssertFieldConstraints;
import net.sf.oval.internal.util.ReflectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Partial implementation of check classes configurable via annotations.
 * 
 * @author Sebastian Thomschke
 */
public abstract class AbstractAnnotationCheck<ConstraintAnnotation extends Annotation> extends AbstractCheck
		implements
			AnnotationCheck<ConstraintAnnotation>
{
	private static final long serialVersionUID = 1L;

	private static final Logger LOG = LoggerFactory.getLogger(AbstractAnnotationCheck.class);

    public void configure(final ConstraintAnnotation constraintAnnotation) {
        configure(getSettings(constraintAnnotation));
    }

    /**
     * Returns value object {@code ConstraintAnnotationSettings} containing the basic settings of the constraint settings
     *
     * @param constraintAnnotation Annotation from which the settings will be extracted
     *
     * @return Value object {@code ConstraintAnnotationSettings}.
     */
    protected abstract ConstraintAnnotationSettings getSettings(final ConstraintAnnotation constraintAnnotation);

    public final void configure(ConstraintAnnotationSettings settings) {
        setMessage(settings.getMessage());
        setAppliesTo(settings.getAppliesTo());
        setErrorCode(settings.getErrorCode());
        setSeverity(settings.getSeverity());
        setProfiles(settings.getProfiles());
        setTarget(settings.getTarget());
        setWhen(settings.getWhen());
    }
}
