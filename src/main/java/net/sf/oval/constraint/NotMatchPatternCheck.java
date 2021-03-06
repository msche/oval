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
import net.sf.oval.internal.util.ArrayUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * @author Sebastian Thomschke
 */
public final class NotMatchPatternCheck extends AbstractAnnotationCheck<NotMatchPattern>
{
	private static final long serialVersionUID = 1L;

	private final List<Pattern> patterns = new ArrayList<>(2);

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void configure(final NotMatchPattern constraintAnnotation)
	{
		super.configure(constraintAnnotation);

		synchronized (patterns)
		{
			patterns.clear();
			final String[] stringPatterns = constraintAnnotation.pattern();
			final int[] f = constraintAnnotation.flags();
			for (int i = 0, l = stringPatterns.length; i < l; i++)
			{
				final int flag = f.length > i ? f[i] : 0;
				final Pattern p = Pattern.compile(stringPatterns[i], flag);
				patterns.add(p);
			}
			requireMessageVariablesRecreation();
		}
	}

    /**
     * Returns value object {@code ConstraintAnnotationSettings} containing the basic settings of the constraint annotation
     *
     * @param constraintAnnotation Annotation from which the settings will be extracted
     *
     * @return Value object {@code ConstraintAnnotationSettings}.
     */
    protected ConstraintAnnotationSettings getSettings(final  NotMatchPattern constraintAnnotation) {

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
	protected Map<String, String> createMessageVariables()
	{
		final Map<String, String> messageVariables = new LinkedHashMap<>(2);
		messageVariables.put("pattern", patterns.size() == 1 ? patterns.get(0).toString() : patterns.toString());
		return messageVariables;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected ConstraintTarget[] getAppliesToDefault()
	{
		return new ConstraintTarget[]{ConstraintTarget.VALUES};
	}

	/**
	 * @return the pattern
	 */
	public Pattern[] getPatterns()
	{
		synchronized (patterns)
		{
			return patterns.toArray(new Pattern[patterns.size()]);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isSatisfied(final Object validatedObject, final Object valueToValidate, final OValContext context,
			final Validator validator)
	{
		if (valueToValidate == null) return true;

		for (final Pattern p : patterns)
			if (p.matcher(valueToValidate.toString()).matches()) return false;
		return true;
	}

	/**
	 * @param pattern the pattern to set
	 */
	public void setPattern(final Pattern pattern)
	{
		synchronized (patterns)
		{
			patterns.clear();
			patterns.add(pattern);
		}
		requireMessageVariablesRecreation();
	}

	/**
	 * @param pattern the pattern to set
	 */
	public void setPattern(final String pattern, final int flags)
	{
		synchronized (patterns)
		{
			patterns.clear();
			patterns.add(Pattern.compile(pattern, flags));
		}
		requireMessageVariablesRecreation();
	}

	/**
	 * @param patterns the patterns to set
	 */
	public void setPatterns(final Collection<Pattern> patterns)
	{
		synchronized (this.patterns)
		{
			this.patterns.clear();
			this.patterns.addAll(patterns);
		}
		requireMessageVariablesRecreation();
	}

	/**
	 * @param patterns the patterns to set
	 */
	public void setPatterns(final Pattern... patterns)
	{
		synchronized (this.patterns)
		{
			this.patterns.clear();
			ArrayUtils.addAll(this.patterns, patterns);
		}
		requireMessageVariablesRecreation();
	}
}
