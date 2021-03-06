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
package net.sf.oval.context;

import net.sf.oval.Validator;
import net.sf.oval.internal.util.SerializableConstructor;
import net.sf.oval.internal.util.StringUtils;

import java.lang.reflect.Constructor;

/**
 * @author Sebastian Thomschke
 */
public final class ConstructorParameterContext extends OValContext
{
    private static final long serialVersionUID = -782861376799538876L;

	private final SerializableConstructor constructor;
	private final int parameterIndex;
	private final String parameterName;

	/**
	 * 
	 * @param constructor
	 * @param parameterIndex
	 */
	public ConstructorParameterContext(final Constructor< ? > constructor, final int parameterIndex,
			final String parameterName)
	{
        super(constructor.getParameterTypes()[parameterIndex]);
		this.constructor = SerializableConstructor.getInstance(constructor);
		this.parameterIndex = parameterIndex;
		this.parameterName = parameterName;
	}

	/**
	 * @return Returns the constructor.
	 */
	public Constructor< ? > getConstructor()
	{
		return constructor.getConstructor();
	}

	/**
	 * @return Returns the parameterIndex.
	 */
	public int getParameterIndex()
	{
		return parameterIndex;
	}

	/**
	 * @return the parameterName
	 */
	public String getParameterName()
	{
		return parameterName;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString()
	{
		return constructor.getDeclaringClass().getName()
				+ "("
				+ StringUtils.implode(constructor.getParameterTypes(), ",")
				+ ") "
				+ Validator.getMessageResolver()
						.getMessage("net.sf.oval.context.ConstructorParameterContext.parameter") + " " + parameterIndex
				+ (parameterName == null || parameterName.length() == 0 ? "" : " (" + parameterName + ")");
	}
}
