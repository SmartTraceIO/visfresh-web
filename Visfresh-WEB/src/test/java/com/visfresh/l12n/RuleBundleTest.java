/**
 *
 */
package com.visfresh.l12n;

import static org.junit.Assert.assertNotNull;
import junit.framework.AssertionFailedError;

import org.junit.Test;

import com.visfresh.entities.AlertRule;
import com.visfresh.entities.AlertType;
import com.visfresh.entities.TemperatureRule;
import com.visfresh.entities.TemperatureUnits;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class RuleBundleTest extends RuleBundle {
    private TemperatureUnits units = TemperatureUnits.Fahrenheit;

    /**
     * Default constructor.
     */
    public RuleBundleTest() {
        super();
    }

    @Test
    public void testAlertBundle() {
        for (final AlertType type : AlertType.values()) {
            if (type.isTemperatureAlert()) {
                final TemperatureRule rule = new TemperatureRule(type);
                //not cumulative
                rule.setCumulativeFlag(false);

                String msg = buildDescription(rule, units);
                assertNotNull(msg);
                assertPlaceholdersResolved(type, msg);

                //cumulative
                rule.setCumulativeFlag(true);

                msg = buildDescription(rule, units);
                assertNotNull(msg);
                assertPlaceholdersResolved(type, msg);
            }
        }

        //Other alerts
        for (final AlertType type : AlertType.values()) {
            if (!type.isTemperatureAlert()) {
                final AlertRule rule = new AlertRule(type);

                final String msg = buildDescription(rule, units);
                assertNotNull(msg);
                assertPlaceholdersResolved(type, msg);
            }
        }
    }

    /**
     * @param type notification issue.
     * @param msg message.
     */
    private void assertPlaceholdersResolved(final AlertType type, final String msg) {
        if (msg.contains("${")) {
            throw new AssertionFailedError("Not all placeholders resolved for message '"
                    + msg +"' of " + type);
        }
    }
}
