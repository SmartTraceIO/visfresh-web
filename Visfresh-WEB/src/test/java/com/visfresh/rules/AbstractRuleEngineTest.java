/**
 *
 */
package com.visfresh.rules;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.visfresh.entities.AlertProfile;
import com.visfresh.entities.AlertType;
import com.visfresh.entities.Company;
import com.visfresh.entities.Device;
import com.visfresh.entities.Location;
import com.visfresh.entities.Shipment;
import com.visfresh.entities.TemperatureRule;
import com.visfresh.entities.TrackerEvent;
import com.visfresh.mpl.services.DeviceDcsNativeEvent;
import com.visfresh.rules.state.DeviceState;
import com.visfresh.rules.state.ShipmentSession;
import com.visfresh.services.RetryableException;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class AbstractRuleEngineTest extends AbstractRuleEngine {
    private final Map<String, DeviceState> deviceStates = new HashMap<>();
    private final Map<String, Device> devices = new HashMap<>();
    private final Map<Long, ShipmentSession> sessions = new HashMap<>();
    private final List<Shipment> savedShipments = new LinkedList<>();
    private final List<TrackerEvent> savedTrackerEvents = new LinkedList<>();

    private Shipment detectedShipment;

    private int numInvoked = 0;
    private Company company;
    /**
     * Default constructor.
     */
    public AbstractRuleEngineTest() {
        super();
    }

    @Before
    public void setUp() {
        this.company = new Company(1l);
        company.setName("JUnit");

        createDevice("98324079870987");
    }

    @Test
    public void testExistsDeviceState() throws RetryableException {
        final Device device = createDevice("3249870239847908");

        final DeviceState state = new DeviceState();
        state.setProperty("key1", "value1");

        saveDeviceState(device.getImei(), state);

        //run
        final DeviceDcsNativeEvent e = new DeviceDcsNativeEvent();
        e.setDate(new Date(System.currentTimeMillis() - 10000));
        e.setType("AUT");
        e.setImei(device.getImei());
        processDcsEvent(e);

        //check tracker event saved
        assertEquals(1, savedTrackerEvents.size());

        assertEquals(1, this.numInvoked);
        assertEquals("value1", getDeviceState(device.getImei()).getProperty("key1"));
    }
    @Test
    public void testNotExistsDeviceState() throws RetryableException {
        final Device device = createDevice("3249870239847908");

        assertNull(getDeviceState(device.getImei()));

        //run
        final DeviceDcsNativeEvent e = new DeviceDcsNativeEvent();
        e.setDate(new Date(System.currentTimeMillis() - 10000));
        e.getLocation().setLatitude(11.12);
        e.getLocation().setLongitude(13.14);
        e.setType("AUT");
        e.setImei(device.getImei());
        processDcsEvent(e);

        final DeviceState state = getDeviceState(device.getImei());
        assertEquals(0, state.getKeys().size());
        assertEquals(1, this.numInvoked);

        //check tracker event saved
        assertEquals(1, savedTrackerEvents.size());

        //check last location
        assertNotNull(state.getLastLocation());
        assertEquals(e.getLocation().getLatitude(), state.getLastLocation().getLatitude(), 0.00001);
        assertEquals(e.getLocation().getLongitude(), state.getLastLocation().getLongitude(), 0.00001);
    }
    @Test
    public void testShipmentState() throws RetryableException {
        final Device device = createDevice("3249870239847908");
        final Shipment s = new Shipment();
        s.setId(77l);
        s.setShipmentDescription("JUnit shipment");
        detectedShipment = s;

        //run
        final DeviceDcsNativeEvent e = new DeviceDcsNativeEvent();
        e.setDate(new Date(System.currentTimeMillis() - 10000));
        e.getLocation().setLatitude(11.12);
        e.getLocation().setLongitude(13.14);
        e.setType("AUT");
        e.setImei(device.getImei());

        processDcsEvent(e);

        //check tracker event saved
        assertEquals(1, savedTrackerEvents.size());

        assertEquals(1, savedShipments.size());
        assertEquals(1, sessions.size());
    }
    @Test
    public void testShipmentStateCache() throws RetryableException {
        final Device device = createDevice("3249870239847908");
        final Shipment s = new Shipment();
        s.setId(77l);
        s.setShipmentDescription("JUnit shipment");
        detectedShipment = s;

        //run
        final DeviceDcsNativeEvent e = new DeviceDcsNativeEvent();
        e.setDate(new Date(System.currentTimeMillis() - 10000));
        e.getLocation().setLatitude(11.12);
        e.getLocation().setLongitude(13.14);
        e.setType("AUT");
        e.setImei(device.getImei());

        suppressNextAlerts(s);
        processDcsEvent(e);

        //check tracker event saved
        assertEquals(0, this.sessionCache.size());
    }
    @Test
    public void testIgnoreBadData() throws RetryableException {
        final Device device = createDevice("3249870239847908");
        final Shipment s = new Shipment();
        s.setId(77l);
        s.setShipmentDescription("JUnit shipment");
        detectedShipment = s;

        final DeviceState state = new DeviceState();
        state.setLastLocation(new Location(0., 0.));
        saveDeviceState(device.getImei(), state);

        //run
        final DeviceDcsNativeEvent e = new DeviceDcsNativeEvent();
        e.setDate(new Date(System.currentTimeMillis() - 10000));
        e.getLocation().setLatitude(11.12);
        e.getLocation().setLongitude(13.14);
        e.setType("AUT");
        e.setImei(device.getImei());
        e.getLocation().setLatitude(100.);
        e.getLocation().setLongitude(100.);

        processDcsEvent(e);

        //check event ignored
        assertEquals(0, numInvoked);

        //test shipment is not saved
        assertEquals(0, savedShipments.size());
        assertEquals(0, savedTrackerEvents.size());

        //test last location is not updated
        final DeviceState st = getDeviceState(device.getImei());
        assertEquals(0.0, st.getLastLocation().getLatitude(), 0.0001);
        assertEquals(0.0, st.getLastLocation().getLongitude(), 0.0001);
    }
    @Test
    public void testAlertYetToFire() {
        final Device device = createDevice("3249870239847908");
        final Shipment s = new Shipment();
        s.setId(77l);
        s.setDevice(device);
        s.setShipmentDescription("JUnit shipment");

        //test not alert profile
        assertEquals(0, getAlertYetFoFire(s).size());

        //test with alert profile, not session
        final AlertProfile p = new AlertProfile();
        s.setAlertProfile(p);

        final TemperatureRule r1 = new TemperatureRule();
        r1.setType(AlertType.CriticalHot);
        r1.setId(1l);
        final TemperatureRule r2 = new TemperatureRule();
        r2.setId(2l);
        r2.setType(AlertType.Hot);

        p.getAlertRules().add(r1);
        p.getAlertRules().add(r2);

        assertEquals(2, getAlertYetFoFire(s).size());

        //test with shipment session
        final ShipmentSession session = new ShipmentSession();
        this.sessions.put(s.getId(), session);

        setProcessedTemperatureRule(session, r2);
        assertEquals(1, getAlertYetFoFire(s).size());
        assertEquals(r1.getId(), getAlertYetFoFire(s).get(0).getId());
    }
    @Test
    public void testAlertFired() {
        final Device device = createDevice("3249870239847908");
        final Shipment s = new Shipment();
        s.setId(77l);
        s.setDevice(device);
        s.setShipmentDescription("JUnit shipment");

        //test not alert profile
        assertEquals(0, getAlertYetFoFire(s).size());

        //test with alert profile, not session
        final AlertProfile p = new AlertProfile();
        s.setAlertProfile(p);

        final TemperatureRule r1 = new TemperatureRule();
        r1.setType(AlertType.CriticalHot);
        r1.setId(1l);
        final TemperatureRule r2 = new TemperatureRule();
        r2.setId(2l);
        r2.setType(AlertType.Hot);

        p.getAlertRules().add(r1);
        p.getAlertRules().add(r2);

        assertEquals(0, getAlertFired(s).size());

        //test with shipment session
        final ShipmentSession session = new ShipmentSession();
        this.sessions.put(s.getId(), session);

        setProcessedTemperatureRule(session, r2);
        assertEquals(1, getAlertFired(s).size());
        assertEquals(r2.getId(), getAlertFired(s).get(0).getId());
    }
    /**
     * @param imei
     */
    protected Device createDevice(final String imei) {
        final Device d = new Device();
        d.setImei(imei);
        d.setActive(true);
        d.setCompany(company);
        d.setName("JUnit");
        d.setDescription("JUnit device");
        devices.put(d.getImei(), d);
        return d;
    }
    /* (non-Javadoc)
     * @see com.visfresh.services.RuleEngine#invokeRules(com.visfresh.rules.RuleContext)
     */
    @Override
    public void invokeRules(final RuleContext context) {
        numInvoked++;
        context.getEvent().setShipment(detectedShipment);
        if (detectedShipment != null) {
            context.getSessionManager().getSession(detectedShipment);
        }
    }

    /* (non-Javadoc)
     * @see com.visfresh.rules.AbstractRuleEngine#findDevice(java.lang.String)
     */
    @Override
    protected Device findDevice(final String imei) {
        return devices.get(imei);
    }
    /* (non-Javadoc)
     * @see com.visfresh.rules.AbstractRuleEngine#getDeviceState(java.lang.String)
     */
    @Override
    protected DeviceState getDeviceState(final String imei) {
        return deviceStates.get(imei);
    }
    /* (non-Javadoc)
     * @see com.visfresh.rules.AbstractRuleEngine#saveDeviceState(java.lang.String, com.visfresh.rules.state.DeviceState)
     */
    @Override
    protected void saveDeviceState(final String imei, final DeviceState state) {
        deviceStates.put(imei, state);
    }
    /* (non-Javadoc)
     * @see com.visfresh.rules.AbstractRuleEngine#loadSession(com.visfresh.entities.Shipment)
     */
    @Override
    protected ShipmentSession loadSessionFromDb(final Shipment s) {
        return sessions.get(s.getId());
    }
    /* (non-Javadoc)
     * @see com.visfresh.rules.AbstractRuleEngine#saveSession(com.visfresh.entities.Shipment, com.visfresh.rules.state.ShipmentSession)
     */
    @Override
    protected void saveSessionToDb(final Shipment s, final ShipmentSession ss) {
        sessions.put(s.getId(), ss);
    }
    /* (non-Javadoc)
     * @see com.visfresh.rules.AbstractRuleEngine#saveShipment(com.visfresh.entities.Shipment)
     */
    @Override
    protected Shipment saveShipment(final Shipment shipment) {
        savedShipments.add(shipment);
        return shipment;
    }
    /* (non-Javadoc)
     * @see com.visfresh.rules.AbstractRuleEngine#saveTrackerEvent(com.visfresh.entities.TrackerEvent)
     */
    @Override
    protected TrackerEvent saveTrackerEvent(final TrackerEvent e) {
        savedTrackerEvents.add(e);
        return e;
    }
}
