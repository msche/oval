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

import net.sf.oval.context.OValContext;

import javax.validation.constraints.Pattern;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;




/**
 * @author Sebastian Thomschke
 */
public final class PatternCheck extends AbstractAnnotationCheck<Pattern>
{
	private static final long serialVersionUID = 1L;

	private final List<java.util.regex.Pattern> patterns = new ArrayList<>(2);
	private boolean matchAll = true;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void configure(final Pattern constraintAnnotation)
	{
		setMessage(constraintAnnotation.message());
		setGroups(constraintAnnotation.groups());

		synchronized (patterns)
		{
			patterns.clear();
			final String stringPatterns = constraintAnnotation.regexp();
			final Pattern.Flag[] flags = constraintAnnotation.flags();

			int f = 0;
			for(int i=0; i<flags.length; i++) {
				f = i | flags[i].getValue();
			}

			patterns.add(java.util.regex.Pattern.compile(stringPatterns, f));
		}
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
	 * @return the matchAll
	 */
	public boolean isMatchAll()
	{
		return matchAll;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isSatisfied(final Object validatedObject, final Object valueToValidate, final OValContext context,
			final Validator validator)
	{
		if (valueToValidate == null) return true;

		for (final java.util.regex.Pattern p : patterns)
		{
			final boolean matches = p.matcher(valueToValidate.toString()).matches();

			if (matches)
			{
				if (!matchAll) return true;
			}
			else if (matchAll) return false;
		}
		return matchAll ? true : false;
	}

	/**
	 * @param pattern the pattern to set
	 */
	public void setPattern(final java.util.regex.Pattern pattern)
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
			patterns.add(java.util.regex.Pattern.compile(pattern, flags));
		}
		requireMessageVariablesRecreation();
	}

	/**
	 * @param patterns the patterns to set
	 */
	public void setPatterns(final Collection<java.util.regex.Pattern> patterns)
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
	public void setPatterns(final java.util.regex.Pattern... patterns)
	{
		synchronized (this.patterns)
		{
			this.patterns.clear();
			Collections.addAll(this.patterns, patterns);
		}
		requireMessageVariablesRecreation();
	}
}
