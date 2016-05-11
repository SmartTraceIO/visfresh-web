/**
 *
 */
package com.visfresh.rules;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Date;

import org.junit.Test;

import com.visfresh.entities.Shipment;
import com.visfresh.entities.TrackerEvent;
import com.visfresh.rules.state.DeviceState;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class CleanShutdownRepeatStateRuleTest extends
        CleanShutdownRepeatStateRule {
    /**
     * Default constructor.
     */
    public CleanShutdownRepeatStateRuleTest() {
        super();
    }

    @Test
    public void testAccept() {
        final Shipment s = new Shipment();
        final TrackerEvent e = new TrackerEvent();
        e.setShipment(s);

        final RuleContext context = new RuleContext(e, new SessionHolder());
        context.setDeviceState(new DeviceState());
        NoInitMessageAfterShutdownRule.setShutDownRepeatTime(context.getDeviceState(), new Date());

        assertTrue(accept(context));

        //test with not shutdown repeat time
        NoInitMessageAfterShutdownRule.setShutDownRepeatTime(context.getDeviceState(), null);
        assertFalse(accept(context));

        NoInitMessageAfterShutdownRule.setShutDownRepeatTime(context.getDeviceState(), new Date());
        assertTrue(accept(context));

        //test with shutdown device time set
        s.setDeviceShutdownTime(new Date());
        assertFalse(accept(context));

        s.setDeviceShutdownTime(null);
        assertTrue(accept(context));

        //test with not shipment set
        e.setShipment(null);
        assertFalse(accept(context));
    }
    @Test
    public void testHandle() {
        final TrackerEvent e = new TrackerEvent();
        final RuleContext context = new RuleContext(e, new SessionHolder());
        context.setDeviceState(new DeviceState());
        NoInitMessageAfterShutdownRule.setShutDownRepeatTime(context.getDeviceState(), new Date());

        assertFalse(handle(context));
        assertNull(NoInitMessageAfterShutdownRule.getShutDownRepeatTime(context.getDeviceState()));
    }
}
