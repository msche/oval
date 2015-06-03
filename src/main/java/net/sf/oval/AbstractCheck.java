/*******************************************************************************
 * Portions created by Sebastian Thomschke are copyright (c) 2005-2013 Sebastian
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
package net.sf.oval;

import net.sf.oval.context.OValContext;
import net.sf.oval.expression.ExpressionLanguage;

import java.io.Serializable;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Partial implementation of check classes.
 *
 * @author Sebastian Thomschke
 */
public abstract class AbstractCheck implements Check
{
	private static final long serialVersionUID = 1L;

	private OValContext context;

	private String message;
	private Map<String, ? extends Serializable> messageVariables;
	private boolean messageVariablesUpToDate = true;

	// TODO: Replace this by classes
	private String[] profiles;

	// TODO remove properties which are not set by javax validation annotations (only message and profiles).
	private ConstraintTarget[] appliesTo;

	protected Map<String, ? extends Serializable> createMessageVariables()
	{
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	public ConstraintTarget[] getAppliesTo()
	{
		return appliesTo == null ? getAppliesToDefault() : appliesTo;
	}

	/**
	 *
	 * @return the default behavior when the constraint is validated for a array/map/collection reference.
	 */
	protected ConstraintTarget[] getAppliesToDefault()
	{
		// default behavior is only validate the array/map/collection reference and not the contained keys/values
		return new ConstraintTarget[]{ConstraintTarget.CONTAINER};
	}

	/**
	 * {@inheritDoc}
	 */
	public OValContext getContext()
	{
		return context;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getMessage()
	{
		/*
		 * if the message has not been initialized (which might be the case when using XML configuration),
		 * construct the string based on this class' name minus the appendix "Check" plus the appendix ".violated"
		 */
		if (message == null)
		{
			final String className = getClass().getName();
			if (className.endsWith("Check"))
				message = className.substring(0, getClass().getName().length() - "Check".length()) + ".violated";
			else
				message = className + ".violated";
		}
		return message;
	}

	/**
	 * Values that are used to fill place holders when rendering the error message.
	 * A key "min" with a value "4" will replace the place holder {min} in an error message
	 * like "Value cannot be smaller than {min}" with the string "4".
	 *
	 * <b>Note:</b> Override {@link #createMessageVariables()} to create and fill the map
	 *
	 * @return an unmodifiable map
	 */
	@SuppressWarnings("javadoc")
	public final Map<String, ? extends Serializable> getMessageVariables()
	{
		if (!messageVariablesUpToDate)
		{
			messageVariables = Collections.unmodifiableMap(createMessageVariables());
			messageVariablesUpToDate = true;
		}
		if (messageVariables == null)
			return null;
		else
			return messageVariables;
	}

	/**
	 * {@inheritDoc}
	 */
	public String[] getGroups()
	{
		return profiles;
	}

	/**
	 * Calling this method indicates that the {@link #createMessageVariables()} method needs to be called before the message
	 * for the next violation of this check is rendered.
	 */
	protected void requireMessageVariablesRecreation()
	{
		messageVariablesUpToDate = false;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setAppliesTo(final ConstraintTarget... targets)
	{
		appliesTo = targets;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setContext(final OValContext context)
	{
		this.context = context;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setMessage(final String message)
	{
		this.message = message;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setGroups(final Class... groups)
	{
		if (groups == null) {
			profiles = null;
		} else {
			profiles = new String[groups.length];
			for (int i = 0; i < groups.length; i++) {
				profiles[i] = groups[i].getCanonicalName();
			}
		}
	}

}
