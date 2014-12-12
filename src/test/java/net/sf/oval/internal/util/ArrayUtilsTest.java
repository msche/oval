package net.sf.oval.internal.util;

import org.junit.Test;

import java.util.List;
import java.util.Random;

import static org.junit.Assert.*;

/**
 * Verifies functionality of {@code ArrayUtils}.
 *
 * @author mase
 * @since 1.8.5
 */
public class ArrayUtilsTest {

    Random randomizer = new Random();

    /**
     * Verifies {@code IllegalArgumentException} is thrown when passed object
     * is null
     */
    @Test(expected=IllegalArgumentException.class)
    public void testAsListNull() {
        ArrayUtils.asList(null);
    }

    /**
     * Verifies array of Objects is properly transformed into list
     */
    @Test
    public void testAsListObject() {
        Object[] array = new Object[randomizer.nextInt(1000)];
        for (int i=0; i< array.length; i++) {
            array[i] = new Object();
        }

        List<Object> result = ArrayUtils.asList((Object) array);

        assertEquals(array.length, result.size());
    }

    /**
     * Verifies array of promitives is properly transformed into list
     */
    @Test
    public void testAsListPromitives() {
        int[] array = new int[randomizer.nextInt(1000)];
        for (int i=0; i< array.length; i++) {
            array[i] = randomizer.nextInt();
        }

        List<Object> result = ArrayUtils.asList((Object) array);

        assertEquals(array.length, result.size());
    }

}
