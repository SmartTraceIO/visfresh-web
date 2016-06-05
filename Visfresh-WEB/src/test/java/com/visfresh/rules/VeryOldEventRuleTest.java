/**
 *
 */
package com.visfresh.rules;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.junit.Test;

import com.visfresh.entities.Shipment;
import com.visfresh.entities.TrackerEvent;
import com.visfresh.entities.TrackerEventType;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class VeryOldEventRuleTest extends VeryOldEventRule {
    private final List<TrackerEvent> savedEvents = new LinkedList<>();

    /**
     * Default constructor.
     */
    public VeryOldEventRuleTest() {
        super();
    }

    @Test
    public void testAccept() {
        final long time = System.currentTimeMillis();

        final TrackerEvent e = new TrackerEvent();
        e.setType(TrackerEventType.AUT);
        e.setTime(new Date(time - 15 * 60 * 1000l));
        e.setCreatedOn(new Date(time));

        //test not accept small inte
        assertFalse(accept(new RuleContext(e, null)));

        e.setTime(new Date(time - MAX_TIME_DIFF - 10l));
        assertTrue(accept(new RuleContext(e, null)));
    }

    @Test
    public void testHandle() {
        final TrackerEvent e = new TrackerEvent();
        e.setType(TrackerEventType.AUT);

        //test not accept small inte
        final RuleContext context = new RuleContext(e, null);
        assertFalse(handle(context));

        assertTrue(context.isEventConsumed());
        assertEquals(0, savedEvents.size());
    }

    @Test
    public void testSetShipmentToNull() {
        final TrackerEvent e = new TrackerEvent();
        e.setType(TrackerEventType.AUT);
        e.setShipment(new Shipment());

        //test not accept small inte
        final RuleContext context = new RuleContext(e, null);
        assertFalse(handle(context));

        assertTrue(context.isEventConsumed());
        assertEquals(1, savedEvents.size());
        assertNull(e.getShipment());
    }
    /* (non-Javadoc)
     * @see com.visfresh.rules.VeryOldEventRule#saveTrackerEvent(com.visfresh.entities.TrackerEvent)
     */
    @Override
    protected void saveTrackerEvent(final TrackerEvent e) {
        savedEvents.add(e);
    }
}
