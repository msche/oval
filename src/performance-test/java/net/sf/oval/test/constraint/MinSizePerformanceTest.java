package net.sf.oval.test.constraint;

import net.sf.oval.constraint.MinSize;
import net.sf.oval.exception.ConstraintsViolatedException;
import net.sf.oval.test.guard.TestGuardAspect;
import org.databene.contiperf.Required;
import net.sf.oval.guard.Guard;
import net.sf.oval.guard.Guarded;
import org.databene.contiperf.PerfTest;
import org.databene.contiperf.junit.ContiPerfRule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Performance test for {@code MinSize}.
 *
 * @author mase
 * @since 1.85
 */
public class MinSizePerformanceTest {

    private static final int MINIMAL_SIZE = 10;

    @Guarded
    protected static class TestSubject
    {
        @MinSize(MINIMAL_SIZE)
        Object[] array;

        @MinSize(MINIMAL_SIZE)
        Map<Object, Object> map;

        @MinSize(MINIMAL_SIZE)
        Collection collection;

        TestSubject(){}

        protected TestSubject(@MinSize(MINIMAL_SIZE) final Object[] value) {}
        protected TestSubject(@MinSize(MINIMAL_SIZE) final Map<Object, Object> value) {}
        protected TestSubject(@MinSize(MINIMAL_SIZE) final Collection value) {}

        public void setValueConstraint(@MinSize(MINIMAL_SIZE) final Object[] value){}
        public void setValueConstraint(@MinSize(MINIMAL_SIZE) final Map<Object,Object> value){}
        public void setValueConstraint(@MinSize(MINIMAL_SIZE) final Collection value){}
    }

    private TestSubject testSubject;

    private final Object[] validArray = new Object[MINIMAL_SIZE];
    private final Map<Object, Object> validMap = new HashMap<>(MINIMAL_SIZE);
    private final Collection validCollection = new ArrayList(MINIMAL_SIZE);

    private final Object[] invalidArray = new Object[0];
    private final Map<Object, Object> invalidMap = new HashMap<>();
    private final Collection invalidCollection = new ArrayList();

    @Rule
    public ContiPerfRule performanceRule = new ContiPerfRule();

    @Before
    public void setup() {
        final Guard guard = new Guard();
        TestGuardAspect.aspectOf().setGuard(guard);

        testSubject = new TestSubject();

        for (int i=0; i<MINIMAL_SIZE; i++) {
            Object value = new Object();
            validArray[i] = value;
            validMap.put(value,value);
            validCollection.add(value);
        }
    }

    /**
     * Measure performance @{MinSize} at constructor when passed valid array
     */
    @Test
    @PerfTest(invocations=10000)
    @Required(average = 1)
    public void performanceTestCreateInstanceValidArray() {
        new TestSubject(validArray);
    }

    /**
     * Measure performance @{MinSize} at constructor when passed invalid array
     */
    @Test(expected = ConstraintsViolatedException.class)
    @PerfTest(invocations=10000)
    @Required(average = 1)
    public void performanceTestCreateInstanceInvalidArray() {
        new TestSubject(invalidArray);
    }

    /**
     * Measure performance @{MinSize} at constructor when passed valid map
     */
    @Test
    @PerfTest(invocations=10000)
    @Required(average = 1)
    public void performanceTestCreateInstanceValidMap() {
        new TestSubject(validMap);
    }

    /**
     * Measure performance @{MinSize} at constructor when passed invalid map
     */
    @Test(expected = ConstraintsViolatedException.class)
    @PerfTest(invocations=10000)
    @Required(average = 1)
    public void performanceTestCreateInstanceInvalidMap() {
        new TestSubject(invalidMap);
    }

    /**
     * Measure performance @{MinSize} at constructor when passed valid collection
     */
    @Test
    @PerfTest(invocations=10000)
    @Required(average = 1)
    public void performanceTestCreateInstanceValidCollection() {
        new TestSubject(validCollection);
    }

    /**
     * Measure performance @{MinSize} at constructor when passed invalid map
     */
    @Test(expected = ConstraintsViolatedException.class)
    @PerfTest(invocations=10000)
    @Required(average = 1)
    public void performanceTestCreateInstanceInvalidCollection() {
        new TestSubject(invalidCollection);
    }
}
