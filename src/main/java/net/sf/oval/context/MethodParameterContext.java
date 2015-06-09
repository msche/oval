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
import net.sf.oval.internal.util.StringUtils;

import java.lang.reflect.Method;

/**
 * @author Sebastian Thomschke
 */
public final class MethodParameterContext extends OValContext
{
	private final Method method;
	private final int parameterIndex;
	private final String parameterName;

	public MethodParameterContext(final Method method, final int parameterIndex, final String parameterName)
	{
        super(method.getParameterTypes()[parameterIndex]);
		this.method = method;
		this.parameterIndex = parameterIndex;
		this.parameterName = parameterName == null ? "param" + parameterIndex : parameterName;
//		this.compileTimeType = method.getParameterTypes()[parameterIndex];
	}

	/**
	 * @return Returns the method.
	 */
	public Method getMethod()
	{
		return method;
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
        return method.getDeclaringClass().getName()
                + "." + method.getName() + "("
                + StringUtils.implode(method.getParameterTypes(), ",")
                + ") " + Validator.getMessageResolver().getMessage("net.sf.oval.context.MethodParameterContext.parameter")
                + " " + parameterIndex + " (" + parameterName + ")";
	}
}
