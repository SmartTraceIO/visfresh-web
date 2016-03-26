/**
 *
 */
package com.visfresh.rules;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.visfresh.entities.InterimStop;
import com.visfresh.entities.Shipment;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class InterimStopRuleTest extends InterimStopRule {
    private long ids = 0;
    private final Map<Long, Integer> stopMinutes = new HashMap<>();

    /**
     * Default constructor.
     */
    public InterimStopRuleTest() {
        super();
    }

    @Test
    public void testAccept() {
        //TODO
    }

    /* (non-Javadoc)
     * @see com.visfresh.rules.InterimStopRule#save(com.visfresh.entities.Shipment, com.visfresh.entities.InterimStop)
     */
    @Override
    protected Long save(final Shipment shipment, final InterimStop stop) {
        stop.setId(++ids);
        return stop.getId();
    }
    /* (non-Javadoc)
     * @see com.visfresh.rules.InterimStopRule#updateStopTime(com.visfresh.rules.InterimStopRule.InterimStopInfo, int)
     */
    @Override
    protected void updateStopTime(final InterimStopInfo stop, final int minutes) {
        stopMinutes.put(stop.getId(), minutes);
    }
}
