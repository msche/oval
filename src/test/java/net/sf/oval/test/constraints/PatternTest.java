package net.sf.oval.test.constraints;

import net.sf.oval.constraint.PatternCheck;

import java.util.regex.Pattern;

public class PatternTest extends AbstractContraintsTest
{
	public void testMatchPattern()
	{
		final PatternCheck check = new PatternCheck();
		super.testCheck(check);

		//check.setMatchAll(true);
		check.setPattern("\\d*", 0);
		assertTrue(check.isSatisfied(null, null, null, null));
		assertTrue(check.isSatisfied(null, "", null, null));
		assertTrue(check.isSatisfied(null, "1234", null, null));
		assertFalse(check.isSatisfied(null, "12.34", null, null));
		assertFalse(check.isSatisfied(null, "12,34", null, null));
		assertFalse(check.isSatisfied(null, "foo", null, null));

		check.setPatterns(Pattern.compile("[1234]*", 0), Pattern.compile("[1256]*", 0));
		assertTrue(check.isSatisfied(null, null, null, null));
		assertTrue(check.isSatisfied(null, "", null, null));
		assertTrue(check.isSatisfied(null, "1212", null, null));
		assertFalse(check.isSatisfied(null, "1234", null, null));
		assertFalse(check.isSatisfied(null, "1256", null, null));
		assertFalse(check.isSatisfied(null, "34", null, null));
		assertFalse(check.isSatisfied(null, "56", null, null));

		//check.setMatchAll(false);
//		assertTrue(check.isSatisfied(null, "1212", null, null));
//		assertTrue(check.isSatisfied(null, "1234", null, null));
//		assertTrue(check.isSatisfied(null, "1256", null, null));
//		assertTrue(check.isSatisfied(null, "34", null, null));
//		assertTrue(check.isSatisfied(null, "56", null, null));
	}
}