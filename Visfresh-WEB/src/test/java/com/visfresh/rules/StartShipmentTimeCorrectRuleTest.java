/**
 *
 */
package com.visfresh.rules;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import com.visfresh.dao.TrackerEventDao;
import com.visfresh.entities.Device;
import com.visfresh.entities.Shipment;
import com.visfresh.entities.ShipmentStatus;
import com.visfresh.entities.TrackerEvent;
import com.visfresh.entities.TrackerEventType;
import com.visfresh.rules.state.DeviceState;
/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class StartShipmentTimeCorrectRuleTest extends BaseRuleTest {
    private TrackerEventRule rule;
    private Shipment shipment;

    /**
     * Default constructor.
     */
    public StartShipmentTimeCorrectRuleTest() {
        super();
    }

    @Before
    public void setUp() {
        //create rule
        rule = engine.getRule(StartShipmentTimeCorrectRule.NAME);

        //create shipment.
        final Device device = createDevice("90324870987");
        shipment = createDefaultShipment(ShipmentStatus.InProgress, device);
    }

    @Test
    public void testAccept() {
       final DeviceState state = new DeviceState();
       final TrackerEvent e = createTrackerEvent();

       //check not accept
       assertFalse(rule.accept(new RuleContext(e, state)));

       state.possibleNewShipment(shipment);
       assertFalse(rule.accept(new RuleContext(e, state)));

       state.setStartShipmentDate(null);
       assertTrue(rule.accept(new RuleContext(e, state)));
    }
    @Test
    public void testHandle() {
        final DeviceState state = new DeviceState();
        final TrackerEvent e = createTrackerEvent();

        state.possibleNewShipment(shipment);
        state.setStartShipmentDate(null);

        assertFalse(rule.handle(new RuleContext(e, state)));
        assertNotNull(state.getStartShipmentDate());
    }

    /**
     * @return
     */
    private TrackerEvent createTrackerEvent() {
        final TrackerEvent e = new TrackerEvent();
        e.setShipment(shipment);
        e.setDevice(shipment.getDevice());
        e.setType(TrackerEventType.AUT);
        return context.getBean(TrackerEventDao.class).save(e);
    }
}
