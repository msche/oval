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
package net.sf.oval.configuration.pojo.elements;

import net.sf.oval.Check;

import java.lang.reflect.Field;
import java.util.List;

/**
 * Contains checks that should be applied to certain field within class
 *
 * @author Sebastian Thomschke
 * @author msche
 */
public final class FieldChecks extends AbstractChecks
{
	private static final long serialVersionUID = 3491614273739310489L;
	
	/**
	 * Field at which these checks apply
	 */
	private final Field field;

	/**
	 * Constructor
	 *
	 * @param field field at which checks apply
	 */
	public FieldChecks(Field field) {
		super(field.getType());
		this.field = field;
	}

	/**
	 * Returns name of field at which checks apply
	 */
	public String getName() {
		return field.getName();
	}

	/**
	 * Returns field at which these checks apply
	 */
	public Field getField() {
		return field;
	}

}
