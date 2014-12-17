package net.sf.oval.test.constraint;

import net.sf.oval.constraint.Future;
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

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;

/**
 * Validates performance {@code Future}.
 */
public class FuturePerformanceTest {

    @Guarded
    protected static class TestSubject
    {
        TestSubject(){}

        protected TestSubject(@Future final Date value)
        {
            // nothing
        }

        protected TestSubject(@Future final Calendar value)
        {
            // nothing
        }

        protected TestSubject(@Future final String value)
        {
            // nothing
        }

        public void setValue(@Future final Calendar date)
        {
            // nothing
        }

        public void setValue(@Future final Date date)
        {
            // nothing
        }

        public void setValue(@Future final String date)
        {
            // nothing
        }

    }

    private TestSubject testSubject;

    private Calendar calendarPast;
    private Date datePast;
    private String dateStringPast;

    private Calendar calendarFuture;
    private Date dateFuture;
    private String dateStringFuture;

    @Rule
    public ContiPerfRule performanceRule = new ContiPerfRule();

    @Before
    public void setup() {
        Random randomizer = new Random();

        final Guard guard = new Guard();
        TestGuardAspect.aspectOf().setGuard(guard);

        testSubject = new TestSubject();

        calendarPast = Calendar.getInstance();
        calendarPast.add(Calendar.HOUR, -5);
        datePast = calendarPast.getTime();
        dateStringPast = DateFormat.getDateTimeInstance().format(datePast);

        calendarFuture = Calendar.getInstance();
        calendarFuture.add(Calendar.HOUR, 5);
        dateFuture = calendarFuture.getTime();
        dateStringFuture = DateFormat.getDateTimeInstance().format(dateFuture);
    }

    /**
     * Measures performance past constraint with tolerance when specified calendar is in future
     */
    @Test
    @PerfTest(invocations=10000)
    @Required(average = 1)
    public void performanceTestSetValueCalendarFuture() {
        testSubject.setValue(calendarFuture);
    }

    /**
     * Measures performance past constraint with tolerance when specified date is in future
     */
    @Test
    @PerfTest(invocations=10000)
    @Required(average = 1)
    public void performanceTestSetValueDateFuture() {
        testSubject.setValue(dateFuture);
    }

    /**
     * Measures performance past constraint with tolerance when specified date string is in future
     */
    @Test
    @PerfTest(invocations=10000)
    @Required(average = 1)
    public void performanceTestSetValueDateStringFuture() {
        testSubject.setValue(dateStringFuture);
    }

    /**
     * Measures performance past constraint with tolerance when specified calendar is in future
     */
    @Test
    @PerfTest(invocations=10000)
    @Required(average = 1)
    public void performanceTestConstructorCalendarFuture() {
        new TestSubject(calendarFuture);
    }

    /**
     * Measures performance past constraint with tolerance when specified date is in future
     */
    @Test
    @PerfTest(invocations=10000)
    @Required(average = 1)
    public void performanceTestConstructorDateFuture() {
        new TestSubject(dateFuture);
    }

    /**
     * Measures performance past constraint with tolerance when specified date string is in future
     */
    @Test
    @PerfTest(invocations=10000)
    @Required(average = 1)
    public void performanceTestConstructorDateStringFuture() {
        new TestSubject(dateStringFuture);
    }

    /**
     * Measures performance past constraint with tolerance when specified calendar is in past
     */
    @Test(expected=ConstraintsViolatedException.class)
    @PerfTest(invocations=10000)
    @Required(average = 1)
    public void performanceTestSetValueCalendarPast() {
        testSubject.setValue(calendarPast);
    }

    /**
     * Measures performance past constraint with tolerance when specified date is in past
     */
    @Test(expected=ConstraintsViolatedException.class)
    @PerfTest(invocations=10000)
    @Required(average = 1)
    public void performanceTestSetValueDatePast() {
        testSubject.setValue(datePast);
    }

    /**
     * Measures performance past constraint with tolerance when specified date string is in past
     */
    @Test(expected=ConstraintsViolatedException.class)
    @PerfTest(invocations=10000)
    @Required(average = 1)
    public void performanceTestSetValueDateStringPast() {
        testSubject.setValue(dateStringPast);
    }

    /**
     * Measures performance past constraint with tolerance when specified calendar is in future
     */
    @Test(expected=ConstraintsViolatedException.class)
    @PerfTest(invocations=10000)
    @Required(average = 1)
    public void performanceTestConstructorCalendarPast() {
        new TestSubject(calendarPast);
    }

    /**
     * Measures performance past constraint with tolerance when specified date is in past
     */
    @Test(expected=ConstraintsViolatedException.class)
    @PerfTest(invocations=10000)
    @Required(average = 1)
    public void performanceTestConstructorDatePast() {
        new TestSubject(datePast);
    }

    /**
     * Measures performance past constraint with tolerance when specified date string is in past
     */
    @Test(expected=ConstraintsViolatedException.class)
    @PerfTest(invocations=10000)
    @Required(average = 1)
    public void performanceTestConstructorDateStringPast() {
        new TestSubject(dateStringPast);
    }

}
