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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * An instance of this class provides detailed information about a single constraint
 * violation that occurred during validation.
 *
 * @author Sebastian Thomschke
 * @author msche
 */
public class ConstraintViolation
{
	private static final Logger LOG = LoggerFactory.getLogger(ConstraintViolation.class);

	private final ConstraintViolation[] causes;
	private final String checkName;
	private final OValContext context;
	private transient Object invalidValue;
	private final String message;
	private final String messageTemplate;

	private transient Object validatedObject;

	public ConstraintViolation(final Check check, final String message, final Object validatedObject, final Object invalidValue,
			final OValContext context)
	{
		this(check, message, validatedObject, invalidValue, context, (ConstraintViolation[]) null);
	}

	public ConstraintViolation(final Check check, final String message, final Object validatedObject, final Object invalidValue,
			final OValContext context, final ConstraintViolation... causes)
	{
		checkName = check.getClass().getName();
		this.message = message;
		messageTemplate = check.getMessage();
		this.validatedObject = validatedObject;
		this.invalidValue = invalidValue;
		this.context = context;
		this.causes = causes != null && causes.length == 0 ? null : causes;
	}

	public ConstraintViolation(final Check check, final String message, final Object validatedObject, final Object invalidValue,
			final OValContext context, final List<ConstraintViolation> causes)
	{
		this(check, message, validatedObject,invalidValue,context, causes == null || causes.size() == 0 ? null : causes.toArray(new ConstraintViolation[causes.size()]));
	}

	/**
	 * @return the causes or null of no causes exists
	 */
	public ConstraintViolation[] getCauses()
	{
		return causes == null ? null : (ConstraintViolation[]) causes.clone();
	}

	/**
	 * @return the fully qualified class name of the corresponding check
	 */
	public String getCheckName()
	{
		return checkName;
	}

	/**
	 * @return Returns the context where the constraint violation occurred.
	 *
	 * @see net.sf.oval.context.ClassContext
	 * @see net.sf.oval.context.FieldContext
	 * @see net.sf.oval.context.MethodEntryContext
	 * @see net.sf.oval.context.MethodExitContext
	 * @see net.sf.oval.context.MethodParameterContext
	 * @see net.sf.oval.context.MethodReturnValueContext
	 */
	public OValContext getContext()
	{
		return context;
	}

	/**
	 * @return Returns the value that was validated.
	 */
	public Object getInvalidValue()
	{
		return invalidValue;
	}

	/**
	 * @return the localized and rendered message
	 */
	public String getMessage()
	{
		return message;
	}

	/**
	 * @return the raw message specified for the constraint without variable resolution and localization
	 */
	public String getMessageTemplate()
	{
		return messageTemplate;
	}

	/**
	 * @return the validatedObject
	 */
	public Object getValidatedObject()
	{
		return validatedObject;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString()
	{
		return getClass().getName() + ": " + message;
	}

}
