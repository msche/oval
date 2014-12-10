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

import java.lang.reflect.Method;
import java.util.*;

/**
 * @author Sebastian Thomschke
 */
public final class ArrayUtils
{
	public static final Method[] EMPTY_METHOD_ARRAY = {};
	public static final Object[] EMPTY_OBJECT_ARRAY = {};
	public static final String[] EMPTY_STRING_ARRAY = {};

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
     *     Collections.addAll(flavors, "Peaches 'n Plutonium", "Rocky Racoon");
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
	public static <T> boolean addAll(final Collection<T> collection, final T... elements)
	{
		if (elements == null) {
            return false;
        } else {
            return Collections.addAll(collection, elements);
        }
	}

	public static List< ? > asList(final Object array)
	{
		if (array instanceof Object[])
		{
			final Object[] arrayCasted = (Object[]) array;
			final List<Object> result = new ArrayList<Object>(arrayCasted.length);
			Collections.addAll(result, arrayCasted);
			return result;
		}
		if (array instanceof byte[])
		{
			final byte[] arrayCasted = (byte[]) array;
			final List<Byte> result = new ArrayList<Byte>(arrayCasted.length);
			for (final byte i : arrayCasted)
				result.add(i);
			return result;
		}
		if (array instanceof char[])
		{
			final char[] arrayCasted = (char[]) array;
			final List<Character> result = new ArrayList<Character>(arrayCasted.length);
			for (final char i : arrayCasted)
				result.add(i);
			return result;
		}
		if (array instanceof short[])
		{
			final short[] arrayCasted = (short[]) array;
			final List<Short> result = new ArrayList<Short>(arrayCasted.length);
			for (final short i : arrayCasted)
				result.add(i);
			return result;
		}
		if (array instanceof int[])
		{
			final int[] arrayCasted = (int[]) array;
			final List<Integer> result = new ArrayList<Integer>(arrayCasted.length);
			for (final int i : arrayCasted)
				result.add(i);
			return result;
		}
		if (array instanceof long[])
		{
			final long[] arrayCasted = (long[]) array;
			final List<Long> result = new ArrayList<Long>(arrayCasted.length);
			for (final long i : arrayCasted)
				result.add(i);
			return result;
		}
		if (array instanceof double[])
		{
			final double[] arrayCasted = (double[]) array;
			final List<Double> result = new ArrayList<Double>(arrayCasted.length);
			for (final double i : arrayCasted)
				result.add(i);
			return result;
		}
		if (array instanceof float[])
		{
			final float[] arrayCasted = (float[]) array;
			final List<Float> result = new ArrayList<Float>(arrayCasted.length);
			for (final float i : arrayCasted)
				result.add(i);
			return result;
		}
		if (array instanceof boolean[])
		{
			final boolean[] arrayCasted = (boolean[]) array;
			final List<Boolean> result = new ArrayList<Boolean>(arrayCasted.length);
			for (final boolean i : arrayCasted)
				result.add(i);
			return result;
		}

		throw new IllegalArgumentException("Argument [array] must be an array");
	}

	public static <T> List<T> asList(final T[] array)
	{
        return new ArrayList<T>(Arrays.asList(array));
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
