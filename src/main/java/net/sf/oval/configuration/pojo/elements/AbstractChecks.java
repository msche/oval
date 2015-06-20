package net.sf.oval.configuration.pojo.elements;

import net.sf.oval.Check;
import net.sf.oval.exception.InvalidConfigurationException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Contains checks for element
 *
 * @author msche
 */
abstract class AbstractChecks {

    /**
     * the type at which the checks apply
     */
    private final Class<?> type;

    /**
     * checks that need to be.
     */
    private final List<Check> checks = new ArrayList();

    AbstractChecks(Class<?> type) {
        if (type == null) {
            throw new InvalidConfigurationException("Type at which check applies may not be null");
        } else {
            this.type = type;
        }
    }
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
        return Collections.unmodifiableList(checks);
    }

    /**
     * Returns type of at which checks are applied
     */
    public Class<?> getType() {
        return type;
    }

    /**
     * Append check for element
     */
    public void addCheck(Check check) {
        if(check != null) {
            if (check.supports(type)) {
                checks.add(check);
            } else {
                throw new InvalidConfigurationException(check.getClass().getCanonicalName() + " cannot be applied to " + type.getCanonicalName());
            }
        }
    }

    /**
     * Append checks for element
     */
    public void addChecks(List<Check> checks) {
        if (checks != null) {
            for(Check check : checks) {
                addCheck(check);
            }
        }
    }

}
