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
import net.sf.oval.internal.util.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * @author Sebastian Thomschke
 */
public final class NotMemberOfCheck extends AbstractAnnotationCheck<NotMemberOf>
{
	private static final long serialVersionUID = 1L;

	private boolean ignoreCase;
	private List<String> members;
	private transient List<String> membersLowerCase;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void configure(final NotMemberOf constraintAnnotation)
	{
		super.configure(constraintAnnotation);
		setIgnoreCase(constraintAnnotation.ignoreCase());
		setMembers(constraintAnnotation.value());
	}

    /**
     * Returns value object {@code ConstraintAnnotationSettings} containing the basic settings of the constraint annotation
     *
     * @param constraintAnnotation Annotation from which the settings will be extracted
     *
     * @return Value object {@code ConstraintAnnotationSettings}.
     */
    protected ConstraintAnnotationSettings getSettings(final  NotMemberOf constraintAnnotation) {

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
		messageVariables.put("ignoreCase", Boolean.toString(ignoreCase));
		messageVariables.put("members", StringUtils.implode(members, ","));
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
	 * @return the members
	 */
	public List<String> getMembers()
	{
		final List<String> v = new ArrayList<>();
		v.addAll(members);
		return v;
	}

	private List<String> getMembersLowerCase()
	{
		if (membersLowerCase == null)
		{
			membersLowerCase = new ArrayList<>(members.size());
			for (final String val : members)
				membersLowerCase.add(val.toLowerCase(Locale.getDefault()));
		}
		return membersLowerCase;
	}

	/**
	 * @return the ignoreCase
	 */
	public boolean isIgnoreCase()
	{
		return ignoreCase;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isSatisfied(final Object validatedObject, final Object valueToValidate, final OValContext context,
			final Validator validator)
	{
		if (valueToValidate == null) return true;

		if (ignoreCase)
			return !getMembersLowerCase().contains(valueToValidate.toString().toLowerCase(Locale.getDefault()));

		return !members.contains(valueToValidate.toString());
	}

	/**
	 * @param ignoreCase the ignoreCase to set
	 */
	public void setIgnoreCase(final boolean ignoreCase)
	{
		this.ignoreCase = ignoreCase;
		requireMessageVariablesRecreation();
	}

	/**
	 * @param members the members to set
	 */
	public void setMembers(final List<String> members)
	{
		this.members = new ArrayList<>();
		this.members.addAll(members);
		membersLowerCase = null;
		requireMessageVariablesRecreation();
	}

	/**
	 * @param members the members to set
	 */
	public void setMembers(final String... members)
	{
		this.members = new ArrayList<>();
		Collections.addAll(this.members, members);
		membersLowerCase = null;
		requireMessageVariablesRecreation();
	}
}
