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
package net.sf.oval.internal.util;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * @author Sebastian Thomschke
 */
public final class ArrayUtils
{
	public static final Object[] EMPTY_OBJECT_ARRAY = {};

    /**
     * Returns a list containing the values in the specified array.
     *
     * @param array array of values
     *
     * @return a list contains the values of the specified array
     */
    public static <T> List<T> asList(final T array)
	{
        Assert.argumentNotNull("array", array);
        if (array.getClass().isArray()) {
            Class arrayType = array.getClass().getComponentType();
            if (arrayType.isPrimitive()) {
                List<T> result = new ArrayList<>();
                final int size = Array.getLength(array);
                for(int i=0; i<size; i++) {
                    result.add((T) Array.get(array,i));
                }
                return result;
            } else {
                return asList((T[]) array);
            }
        } else {
            throw new IllegalArgumentException("Argument [array] must be an array");
        }
	}

	public static <T> List<T> asList(final T[] array)
	{
        Assert.argumentNotNull("array", array);
        return new ArrayList<>(Arrays.asList(array));
	}

	public static <T> boolean containsEqual(final T[] theArray, final T theItem)
	{
		for (final T t : theArray)
		{
			if (t == theItem) return true;
			if (t != null && t.equals(theItem)) return true;
		}
		return false;
	}

	public static <T> boolean containsSame(final T[] theArray, final T theItem)
	{
		for (final T t : theArray)
			if (t == theItem) return true;
		return false;
	}

	private ArrayUtils()
	{
		super();
	}
}
