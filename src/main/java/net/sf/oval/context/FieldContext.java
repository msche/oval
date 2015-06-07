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

import net.sf.oval.internal.util.ReflectionUtils;
import net.sf.oval.internal.util.SerializableField;

import java.lang.reflect.Field;

/**
 * @author Sebastian Thomschke
 */
public final class FieldContext extends OValContext
{
    private static final long serialVersionUID = 4670700839313440381L;

	private final SerializableField field;

	/**
	 * @param field
	 */
	public FieldContext(final Field field)
	{
        super(field.getType());
		this.field = SerializableField.getInstance(field);
	}

	/**
	 * @return Returns the field.
	 */
	public Field getField()
	{
		return field.getField();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString()
	{
		return field.getDeclaringClass().getName() + "." + field.getName();
	}
}
