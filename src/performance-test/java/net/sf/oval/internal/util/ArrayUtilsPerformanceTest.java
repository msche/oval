package net.sf.oval.internal.util;

import org.databene.contiperf.PerfTest;
import org.databene.contiperf.Required;
import org.databene.contiperf.junit.ContiPerfRule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.util.Random;

/**
 * Performance test for {@code ArrayUtils}.
 *
 * @author mase
 * @since 1.8.5
 */
public class ArrayUtilsPerformanceTest {

    private int[] primitiveArray = new int[1000];
    private Object[] objectArray = new Object[1000];

    @Rule
    public ContiPerfRule performanceRule = new ContiPerfRule();

    @Before
    public void setup() {
        Random randomizer = new Random();

        // Create array of primitives
        for (int i=0; i<primitiveArray.length; i++) {
            primitiveArray[i] = randomizer.nextInt();
        }

        // Create array of objects
        for(int i=0; i<objectArray.length; i++) {
            objectArray[i] = new Object();
        }
    }

    /**
     * Measure performance translating object array into list
     */
    @Test
    @PerfTest(invocations=10000)
    @Required(average = 1)
    public void performanceTestAsListObjectArray() {
        ArrayUtils.asList(objectArray);
    }

    /**
     * Measure performance translating object array into list
     */
    @Test
    @PerfTest(invocations=10000)
    @Required(average = 1)
    public void performanceTestAsListObjects() {
        ArrayUtils.asList((Object) objectArray);
    }

    /**
     * Measure performance translating object array into list
     */
    @Test
    @PerfTest(invocations=10000)
    @Required(average = 1)
    public void performanceTestAsListPrimitives() {
        ArrayUtils.asList((Object) primitiveArray);
    }

    /**
     * Measure performance translating primitive array into list
     */
    @Test
    @PerfTest(invocations=10000)
    @Required(average = 1)
    public void performanceTestAsListPrimtiveArray() {
        ArrayUtils.asList(primitiveArray);
    }

}
