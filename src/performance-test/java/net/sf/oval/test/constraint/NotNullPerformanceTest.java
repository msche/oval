package net.sf.oval.test.constraint;

import javax.validation.constraints.NotNull;
import net.sf.oval.exception.ConstraintsViolatedException;
import net.sf.oval.guard.Guard;
import net.sf.oval.guard.Guarded;
import net.sf.oval.test.guard.TestGuardAspect;
import org.databene.contiperf.PerfTest;
import org.databene.contiperf.Required;
import org.databene.contiperf.junit.ContiPerfRule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

/**
 * Performance test for {@code NotNullCheck}.
 *
 * @author mase
 * @since 1.85
 */
public class NotNullPerformanceTest {

    @Guarded
    protected static class TestSubject
    {
        TestSubject(){}

        protected TestSubject(@NotNull final Object value)
        {
            // nothing
        }

        public void setValueConstraint(@NotNull final Object value)
        {
            // nothing
        }
    }

    private TestSubject testSubject;
    private Object value;

    @Rule
    public ContiPerfRule performanceRule = new ContiPerfRule();

    @Before
    public void setup() {
        final Guard guard = new Guard();
        TestGuardAspect.aspectOf().setGuard(guard);

        testSubject = new TestSubject();
        value = new Object();
    }

    /**
     * Measures performance not null check when passed value method is null
     */
    @Test(expected=ConstraintsViolatedException.class)
    @PerfTest(invocations=10000)
    @Required(average = 1)
    public void performanceTestSetValueNull() {
        testSubject.setValueConstraint(null);
    }

    /**
     * Measures performance not null check when passed value method is not null
     */
    @Test
    @PerfTest(invocations=10000)
    @Required(average = 1)
    public void performanceTestSetValueNotNull() {
        testSubject.setValueConstraint(value);
    }

    /**
     * Measure performance not null check when applied to parameter of contructor.
     */
    @Test(expected=ConstraintsViolatedException.class)
    @PerfTest(invocations=10000)
    @Required(average = 1)
    public void performanceTestCreateInstanceValueNull() {
        new TestSubject(null);
    }

    /**
     * Measure performance not null check when applied to parameter of contructor.
     */
    @Test
    @PerfTest(invocations=10000)
    @Required(average = 1)
    public void performanceTestCreateInstanceValueNotNull() {
        new TestSubject(value);
    }

//    /**
//     * Measure performance not null check when applied to property of instance
//     */
//    @Test
//    @PerfTest(invocations=10000)
//    @Required(average = 1)
//    public void performanceTestPropertyNull() {
//        testSubject.value = null;
//    }
//
//    /**
//     * Measure performance not null check when applied to property of instance
//     */
//    @Test
//    @PerfTest(invocations=10000)
//    @Required(average = 1)
//    public void performanceTestProperty() {
//        testSubject.value = value;
//    }
//
}
