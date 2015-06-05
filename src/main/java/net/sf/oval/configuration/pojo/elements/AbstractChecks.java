package net.sf.oval.configuration.pojo.elements;

import net.sf.oval.Check;

import java.util.ArrayList;
import java.util.List;

/**
 * Contains checks for element
 *
 * @author msche
 */
abstract class AbstractChecks extends ConfigurationElement {

    /**
     * checks for a method's return value that need to be verified after method execution
     */
    private final List<Check> checks = new ArrayList();

    /**
     * Returns whether there are checks for the element.
     */
    public boolean hasChecks() {
        return !checks.isEmpty();
    }

    /**
     * Returns checks that apply to element.
     */
    public List<Check> getChecks() {
        return checks;
    }

    /**
     * Append check for element
     */
    public void addCheck(Check check) {
        checks.add(check);
    }

    /**
     * Append checks for element
     */
    public void addChecks(List<Check> returnValueChecks) {
        checks.addAll(returnValueChecks);
    }

}
