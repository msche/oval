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

import java.lang.reflect.Method;

/**
 * @author Sebastian Thomschke
 */
public final class MethodReturnValueContext extends OValContext
{
	private final Method method;

	public MethodReturnValueContext(final Method method)
	{
        super(method.getReturnType());
		this.method = method;
//		this.compileTimeType = method.getReturnType();
	}

	/**
	 * @return Returns the getter.
	 */
	public Method getMethod()
	{
		return method;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString()
	{
		return method.getDeclaringClass().getName() + "." + method.getName() + "()";
	}
}
