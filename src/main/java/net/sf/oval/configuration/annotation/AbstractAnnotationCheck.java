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

import net.sf.oval.AbstractCheck;
import net.sf.oval.constraint.Assert;
import net.sf.oval.internal.util.CollectionType;
import net.sf.oval.internal.util.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.Map;

/**
 * Partial implementation of check classes configurable via annotations.
 *
 * @author Sebastian Thomschke
 */
public abstract class AbstractAnnotationCheck<T extends Annotation> extends AbstractCheck
        implements
        AnnotationCheck<T> {
    private static final long serialVersionUID = 1L;

    private static final Logger LOG = LoggerFactory.getLogger(AbstractAnnotationCheck.class);

    /**
     * {@inheritDoc}
     */
    public abstract void configure(final T constraintAnnotation);

    /**
     * Verifies whether the type at which the check will be applied is supported
     *
     * @param type
     */
    @Override
    public boolean supports(Class<?> type) {
        return CollectionUtils.getType(type) == CollectionType.SINGLE;
    }


}
