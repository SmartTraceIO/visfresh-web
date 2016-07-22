/**
 *
 */
package com.visfresh.rules;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.visfresh.entities.AutoStartShipment;
import com.visfresh.entities.Device;
import com.visfresh.entities.LocationProfile;
import com.visfresh.entities.Shipment;
import com.visfresh.entities.ShipmentStatus;
import com.visfresh.entities.ShortTrackerEvent;
import com.visfresh.entities.TrackerEvent;
import com.visfresh.entities.TrackerEventType;
import com.visfresh.rules.state.DeviceState;
import com.visfresh.rules.state.LeaveLocationState;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class AutoStartOnLeaveStartLocationRuleTest extends
        AutoStartOnLeaveStartLocationRule {
    private final List<AutoStartShipment> autoStarts = new LinkedList<>();
    private final List<Shipment> shipmentsToAutostart = new LinkedList<>();
    private final List<TrackerEvent> events = new LinkedList<>();
    private long id;
    private Device device;
    private LocationProfile location;

    /**
     * Default constructor.
     */
    public AutoStartOnLeaveStartLocationRuleTest() {
        super();
    }

    @Before
    public void setUp() {
        location = createLocation(77, 78);

        final AutoStartShipment auto = new AutoStartShipment();
        auto.setId(id++);
        auto.setStartOnLeaveLocation(true);
        auto.getShippedFrom().add(location);
        autoStarts.add(auto);

        final Device d = new Device();
        d.setImei("23492843987987");
        d.setActive(true);
        d.setName("Test device");
        d.setAutostartTemplateId(auto.getId());
        this.device = d;
    }

    @Test
    public void testFirstTimeAcceptShipment() {
        final TrackerEvent e = createEvent(location.getLocation().getLatitude(), location.getLocation().getLongitude());
        assertTrue(accept(createContext(e)));

        final Shipment shipment = new Shipment();
        shipment.setStatus(ShipmentStatus.InProgress);
        e.setShipment(shipment);

        assertFalse(accept(createContext(e)));

        shipment.setStatus(ShipmentStatus.Arrived);
        assertTrue(accept(createContext(e)));
    }
    @Test
    public void testFirstTimeNextReading() {
        final TrackerEvent e = createEvent(location.getLocation().getLatitude(), location.getLocation().getLongitude());
        final RuleContext context = createContext(e);
        assertTrue(accept(context));

        saveLeaveLocationState(new LeaveLocationState(location), context.getDeviceState());
        assertFalse(accept(context));
    }
    @Test
    public void testFirstTimeAcceptLocation() {
        final TrackerEvent e = createEvent(location.getLocation().getLatitude(), location.getLocation().getLongitude());
        assertTrue(accept(createContext(e)));

        e.setLatitude(e.getLatitude() + 10);
        e.setLongitude(e.getLongitude() + 10);
        assertFalse(accept(createContext(e)));
    }
    @Test
    public void testFirstTimeAutoStartTemplate() {
        final TrackerEvent e = createEvent(location.getLocation().getLatitude(), location.getLocation().getLongitude());

        final AutoStartShipment auto = findAutoStart(e.getDevice().getAutostartTemplateId());
        auto.setStartOnLeaveLocation(false);
        assertFalse(accept(createContext(e)));

        auto.setStartOnLeaveLocation(true);
        assertTrue(accept(createContext(e)));

        e.getDevice().setAutostartTemplateId(null);
        assertFalse(accept(createContext(e)));
    }
    @Test
    public void testSecondTimeAcceptShipment() {
        final TrackerEvent e = createEvent(
                location.getLocation().getLatitude() + 10,
                location.getLocation().getLongitude() + 10);
        final RuleContext context = createContext(e);
        saveLeaveLocationState(new LeaveLocationState(location), context.getDeviceState());

        assertTrue(accept(context));

        final Shipment shipment = new Shipment();
        shipment.setStatus(ShipmentStatus.InProgress);
        e.setShipment(shipment);

        assertFalse(accept(context));

        shipment.setStatus(ShipmentStatus.Arrived);
        assertTrue(accept(context));
    }
    @Test
    public void testSecondTimeAcceptLocation() {
        final TrackerEvent e = createEvent(
                location.getLocation().getLatitude() + 10,
                location.getLocation().getLongitude() + 10);
        final RuleContext context = createContext(e);
        saveLeaveLocationState(new LeaveLocationState(location), context.getDeviceState());

        assertTrue(accept(context));

        e.setLatitude(location.getLocation().getLatitude());
        e.setLongitude(location.getLocation().getLongitude());
        assertFalse(accept(context));
    }
    @Test
    public void testSecondTimeAutoStartTemplate() {
        final TrackerEvent e = createEvent(
                location.getLocation().getLatitude() + 10,
                location.getLocation().getLongitude() + 10);
        final RuleContext context = createContext(e);
        saveLeaveLocationState(new LeaveLocationState(location), context.getDeviceState());

        final AutoStartShipment auto = findAutoStart(e.getDevice().getAutostartTemplateId());
        auto.setStartOnLeaveLocation(false);
        assertFalse(accept(context));

        auto.setStartOnLeaveLocation(true);
        assertTrue(accept(context));

        e.getDevice().setAutostartTemplateId(null);
        assertFalse(accept(context));
    }
    @Test
    public void testNotAcceptNullCoordinates() {
        final TrackerEvent e = createEvent(null, null);
        final RuleContext context = createContext(e);

        assertFalse(accept(context));

        saveLeaveLocationState(new LeaveLocationState(location), context.getDeviceState());
        assertFalse(accept(context));
    }

    @Test
    public void testHandleStartWatch() {
        final TrackerEvent e = createEvent(location.getLocation().getLatitude(), location.getLocation().getLongitude());
        final RuleContext context = createContext(e);

        assertFalse(handle(context));
        final LeaveLocationState lls = getLeaveLocationState(context.getDeviceState());
        assertNull(lls.getLeaveOn());
    }
    @Test
    public void testHandleJustLeaveLocation() {
        final TrackerEvent e = createEvent(location.getLocation().getLatitude(), location.getLocation().getLongitude());
        final RuleContext context = createContext(e);

        assertFalse(handle(context));

        e.setLatitude(e.getLatitude() + 10);
        e.setLongitude(e.getLongitude() + 10);

        assertFalse(handle(context));
        final LeaveLocationState lls = getLeaveLocationState(context.getDeviceState());
        assertNotNull(lls.getLeaveOn());

        //test ignore repeat
        assertFalse(handle(context));
        assertNotNull(getLeaveLocationState(context.getDeviceState()));
    }
    @Test
    public void testHandleOutsideLocation() {
        final Shipment shipment = new Shipment();
        shipment.setId(id++);
        this.shipmentsToAutostart.add(shipment);

        TrackerEvent e = createEvent(location.getLocation().getLatitude(), location.getLocation().getLongitude());
        e.setTime(new Date(e.getTime().getTime() - CHECK_TIMEOUT - 1000));
        RuleContext context = createContext(e);
        final DeviceState state = context.getDeviceState();

        //simulate first check
        handle(context);
        assertNull(e.getShipment());

        //just leaving the location
        e = createEvent(location.getLocation().getLatitude() + 10, location.getLocation().getLongitude() + 10);
        e.setTime(new Date(e.getTime().getTime() - CHECK_TIMEOUT - 100));
        context = createContext(e);
        context.setDeviceState(state);

        assertFalse(handle(context));
        assertNull(e.getShipment());

        e = createEvent(location.getLocation().getLatitude() + 10, location.getLocation().getLongitude() + 10);
        context = createContext(e);
        context.setDeviceState(state);

        assertTrue(handle(context));
        assertNull(events.get(0).getShipment());
        assertNotNull(events.get(1).getShipment());
        assertNotNull(events.get(2).getShipment());
        assertNull(getLeaveLocationState(state));
    }

    /**
     * @param autostartTemplateId
     * @return
     */
    @Override
    protected AutoStartShipment findAutoStart(final Long autostartTemplateId) {
        for (final AutoStartShipment au : autoStarts) {
            if (au.getId().equals(autostartTemplateId)) {
                return au;
            }
        }
        return null;
    }
    /**
     * @param device device.
     * @param latitude latitude of location.
     * @param longitude longitude of location.
     * @param date autostart date.
     * @return
     */
    @Override
    protected Shipment autoStartNewShipment(final Device device, final double latitude, final double longitude, final Date date) {
        return shipmentsToAutostart.size() == 0 ? null : shipmentsToAutostart.remove(0);
    }
    /**
     * @param device device IMEI.
     * @param startDate start date.
     * @param endDate end date.
     * @return
     */
    @Override
    protected List<ShortTrackerEvent> findTrackerEvents(final String device,
            final Date startDate, final Date endDate) {
        final List<ShortTrackerEvent> result = new LinkedList<>();
        for (final TrackerEvent e : events) {
            final Date date = e.getTime();
            if ((startDate == null || !startDate.after(date))
                    && (endDate == null || !endDate.before(date))) {
                final ShortTrackerEvent se = new ShortTrackerEvent();
                se.setBattery(e.getBattery());
                se.setCreatedOn(e.getCreatedOn());
                se.setDeviceImei(e.getDevice().getImei());
                se.setId(e.getId());
                se.setLatitude(e.getLatitude());
                se.setLongitude(e.getLongitude());
                se.setShipmentId(e.getShipment() == null ? null : e.getShipment().getId());
                se.setTemperature(e.getTemperature());
                se.setTime(e.getTime());
                se.setType(e.getType());
                result.add(se);
            }
        }
        return result;
    }
    /**
     * @param id tracker event ID.
     * @param s shipment to assign.
     */
    @Override
    protected void assignShipment(final Long id, final Shipment s) {
        for (final TrackerEvent e : events) {
            if (e.getId().equals(id)) {
                e.setShipment(s);
                return;
            }
        }
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
        e.setId(id++);
        events.add(e);
        return e;
    }
    /**
     * @return location.
     */
    private LocationProfile createLocation(final double lat, final double lon) {
        final LocationProfile loc = new LocationProfile();
        loc.setAddress("SPb");
        loc.setRadius(700);
        loc.setName("JUnit Start");
        loc.getLocation().setLatitude(lat);
        loc.getLocation().setLongitude(lon);
        loc.setId(id++);
        return loc;
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
