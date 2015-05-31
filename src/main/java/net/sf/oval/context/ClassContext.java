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

/**
 * Validation context for {@code Class}.
 *
 * @author Sebastian Thomschke
 */
public final class ClassContext extends OValContext
{
    private static final long serialVersionUID = 8816732558933947341L;

	//private final Class< ? > clazz;

	public ClassContext(final Class< ? > clazz)
	{
        super(clazz);
		//this.clazz = clazz;
	}

	/**
     * {@code Class} at which this context applies.
     *
	 * @return the clazz
	 */
	public Class< ? > getClazz()
	{
		return getCompileTimeType();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString()
	{
		return getCompileTimeType().getName();
	}
}
