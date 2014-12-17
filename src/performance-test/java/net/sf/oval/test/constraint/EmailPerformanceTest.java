package net.sf.oval.test.constraint;

import net.sf.oval.constraint.Email;
import net.sf.oval.constraint.Future;
import net.sf.oval.exception.ConstraintsViolatedException;
import net.sf.oval.guard.Guard;
import net.sf.oval.test.guard.TestGuardAspect;
import net.sf.oval.guard.Guarded;
import org.databene.contiperf.PerfTest;
import org.databene.contiperf.Required;
import org.databene.contiperf.junit.ContiPerfRule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.util.Calendar;
import java.util.Date;
import java.util.Random;

/**
 * Validates performance of {@code Email}.
 *
 * @since 1.85
 * @author mase
 */
public class EmailPerformanceTest {

    @Guarded
    protected static final class TestSubject
    {
        TestSubject(){}

        protected TestSubject(@Email final String value)
        {
            // nothing
        }

        public void setValue(@Email final String date)
        {
            // nothing
        }

    }

    private TestSubject testSubject;
    private static final String VALID_EMAIL="test@uniknow.com";
    private static final String INVALID_EMAIL = "!test!@uniknown.com";

    @Rule
    public ContiPerfRule performanceRule = new ContiPerfRule();

    @Before
    public void setup() {

        final Guard guard = new Guard();
        TestGuardAspect.aspectOf().setGuard(guard);

        testSubject = new TestSubject();
    }

    /**
     * Measures performance past constraint with tolerance when specified calendar is in future
     */
    @Test
    @PerfTest(invocations=10000)
    @Required(average = 1)
    public void performanceTestSetValueValidEmail() {
        testSubject.setValue(VALID_EMAIL);
    }

    /**
     * Measures performance past constraint with tolerance when specified calendar is in future
     */
    @Test(expected=ConstraintsViolatedException.class)
    @PerfTest(invocations=10000)
    @Required(average = 1)
    public void performanceTestSetValueInvalidEmail() {
        testSubject.setValue(INVALID_EMAIL);
    }

    /**
     * Measures performance past constraint with tolerance when specified calendar is in future
     */
    @Test
    @PerfTest(invocations=10000)
    @Required(average = 1)
    public void performanceTestConstructorValidEmail() {
        new TestSubject(VALID_EMAIL);
    }

    /**
     * Measures performance past constraint with tolerance when specified calendar is in future
     */
    @Test(expected=ConstraintsViolatedException.class)
    @PerfTest(invocations=10000)
    @Required(average = 1)
    public void performanceTestConstructorInvalidEmail() {
        new TestSubject(INVALID_EMAIL);
    }

}
