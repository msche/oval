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
import java.lang.reflect.Method;
import java.util.*;

/**
 * @author Sebastian Thomschke
 */
public final class ArrayUtils
{
	public static final Object[] EMPTY_OBJECT_ARRAY = {};

	/**
     * Adds all of the specified elements to the specified collection.
     * Elements to be added may be specified individually or as an array.
     * The behavior of this convenience method is identical to that of
     * <tt>c.addAll(Arrays.asList(elements))</tt>, but this method is likely
     * to run significantly faster under most implementations.
     *
     * <p>When elements are specified individually, this method provides a
     * convenient way to add a few elements to an existing collection:
     * <pre>
     *     Collections.addAll(flavors, "Peaches 'n Plutonium", "Rocky Raccoon");
     * </pre>
     *
     * @param collection the collection into which <tt>elements</tt> are to be inserted
     * @param elements the elements to insert into <tt>collection</tt>
     * @return <tt>true</tt> if the collection changed as a result of the call
     * @throws UnsupportedOperationException if <tt>c</tt> does not support
     *         the <tt>add</tt> operation
     * @throws NullPointerException if <tt>elements</tt> contains one or more
     *         null values and <tt>c</tt> does not permit null elements, or
     *         if <tt>c</tt> or <tt>elements</tt> are <tt>null</tt>
     * @throws IllegalArgumentException if some property of a value in
     *         <tt>elements</tt> prevents it from being added to <tt>c</tt>
	 */
	@SafeVarargs
    public static <T> boolean addAll(final Collection<T> collection, final T... elements)
	{
        return elements != null && Collections.addAll(collection, elements);
	}

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
