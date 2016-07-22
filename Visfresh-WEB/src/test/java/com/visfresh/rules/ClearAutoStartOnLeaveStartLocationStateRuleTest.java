/**
 *
 */
package com.visfresh.rules;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Date;

import org.junit.Before;
import org.junit.Test;

import com.visfresh.entities.Device;
import com.visfresh.entities.Shipment;
import com.visfresh.entities.ShipmentStatus;
import com.visfresh.entities.TrackerEvent;
import com.visfresh.entities.TrackerEventType;
import com.visfresh.rules.state.DeviceState;
import com.visfresh.rules.state.LeaveLocationState;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class ClearAutoStartOnLeaveStartLocationStateRuleTest extends
        ClearAutoStartOnLeaveStartLocationStateRule {
    private Device device;

    /**
     * Default constructor.
     */
    public ClearAutoStartOnLeaveStartLocationStateRuleTest() {
        super();
    }

    @Before
    public void setUp() {
        device = new Device();
        device.setImei("2093847984237");
        device.setName("JUnit Device");
    }

    @Test
    public void testAcceptForNotFinalShipment() {
        final TrackerEvent e = createEvent(10., 10.);
        final Shipment s = new Shipment();
        s.setStatus(ShipmentStatus.InProgress);
        e.setShipment(s);

        final RuleContext context = createContext(e);
        assertFalse(accept(context));

        //set start to watch
        final LeaveLocationState lls = new LeaveLocationState();
        lls.setName("Test");//set any name for suppress serialization errors
        lls.setLocationId(77l);//set any location ID for suppress serialization errors
        AutoStartOnLeaveStartLocationRule.saveLeaveLocationState(
                lls, context.getDeviceState());

        //assert not accept not leaved the start location.
        assertTrue(accept(context));
    }
    @Test
    public void testAcceptForReturnedToStartLocation() {
        final TrackerEvent e = createEvent(10., 10.);

        final RuleContext context = createContext(e);

        //set start to watch
        final LeaveLocationState lls = new LeaveLocationState();
        lls.setName("Test");//set any name for suppress serialization errors
        lls.setLocationId(77l);//set any location ID for suppress serialization errors
        lls.setLatitude(e.getLatitude());
        lls.setLongitude(e.getLongitude());
        lls.setLeaveOn(new Date(System.currentTimeMillis() - 100000l));
        lls.setRadius(100);
        AutoStartOnLeaveStartLocationRule.saveLeaveLocationState(
                lls, context.getDeviceState());

        //test accept
        assertTrue(accept(context));

        //test not accept not leaving start location
        lls.setLeaveOn(null);
        AutoStartOnLeaveStartLocationRule.saveLeaveLocationState(
                lls, context.getDeviceState());
        assertFalse(accept(context));

        //revert to correct.
        lls.setLeaveOn(new Date(System.currentTimeMillis() - 100000l));
        AutoStartOnLeaveStartLocationRule.saveLeaveLocationState(
                lls, context.getDeviceState());
        assertTrue(accept(context));

        //test not accept out of start location
        lls.setLatitude(e.getLatitude() + 10);
        lls.setLongitude(e.getLongitude() + 10);
        AutoStartOnLeaveStartLocationRule.saveLeaveLocationState(
                lls, context.getDeviceState());
        assertFalse(accept(context));

        //revert to correct
        lls.setLatitude(e.getLatitude());
        lls.setLongitude(e.getLongitude());
        AutoStartOnLeaveStartLocationRule.saveLeaveLocationState(
                lls, context.getDeviceState());
        assertTrue(accept(context));

        //check with set up shipment
        final Shipment s = new Shipment();
        s.setStatus(ShipmentStatus.Arrived);
        e.setShipment(s);
        assertTrue(accept(context));
    }
    @Test
    public void testHandle() {
        final TrackerEvent e = createEvent(10., 10.);

        final RuleContext context = createContext(e);

        //create state
        final LeaveLocationState lls = new LeaveLocationState();
        lls.setName("Test");//set any name for suppress serialization errors
        lls.setLocationId(77l);//set any location ID for suppress serialization errors
        lls.setLatitude(e.getLatitude());
        lls.setLongitude(e.getLongitude());
        lls.setLeaveOn(new Date(System.currentTimeMillis() - 100000l));
        lls.setRadius(100);
        AutoStartOnLeaveStartLocationRule.saveLeaveLocationState(
                lls, context.getDeviceState());

        assertFalse(handle(context));
        assertNull(AutoStartOnLeaveStartLocationRule.getLeaveLocationState(context.getDeviceState()));
    }

    /**
     * @param lat latitude.
     * @param lon longitude
     * @return tracker event with given latitude longitude.
     */
    private TrackerEvent createEvent(final Double lat, final Double lon) {
        final TrackerEvent e = new TrackerEvent();
        e.setDevice(device);
        e.setTime(new Date());
        e.setType(TrackerEventType.AUT);
        e.setLatitude(lat);
        e.setLongitude(lon);
        return e;
    }
    /**
     * @param e
     * @return
     */
    private RuleContext createContext(final TrackerEvent e) {
        final RuleContext c = new RuleContext(e, new SessionHolder());
        c.setDeviceState(new DeviceState());
        return c;
    }
}
