/**
 *
 */
package au.smarttrace.bt04;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import au.smarttrace.Beacon;
import au.smarttrace.DeviceMessage;
import au.smarttrace.GatewayBinding;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class Bt04ServiceTest extends Bt04Service {
    private Beacon beacon;
    private Map<String, Beacon> beacons = new HashMap<>();
    private long lastId;
    private final List<DeviceMessage> messages = new LinkedList<>();
    private final List<String> alerts = new LinkedList<>();
    private final Set<String> locks = new HashSet<>();

    /**
     * Default constructor.
     */
    public Bt04ServiceTest() {
        super();
    }
    @Before
    public void setUp() {
        this.beacon = createBeaconDevice("1289374698773");
    }

    /**
     * @param imei
     */
    private Beacon createBeaconDevice(final String imei) {
        final Beacon d = new Beacon();
        d.setImei(imei);
        d.setActive(true);
        d.setCompany(77777l);

        beacons.put(d.getImei(), d);
        return d;
    }

    @Test
    public void testProcess() {
        final double accuracy = 11.11;
        final double latitude = 22.22;
        final double longitude = 33.33;
        final double altitude = 44.44;
        final Date time = new Date(System.currentTimeMillis() - 10000000l);

        final Bt04Message msg = new Bt04Message();
        msg.setImei(beacon.getImei());
        msg.setAccuracy(accuracy);
        msg.setLatitude(latitude);
        msg.setLongitude(longitude);
        msg.setAltitude(altitude);
        msg.setTime(time);

        //beacons
        final String sn = "beacon-ID";
        final int battery = 50;
        final double distance = 100.;
        final String hardwareModel = "M123";
        final double humidity = 45.88;
        final Date lastScannedTime = new Date(System.currentTimeMillis() - 239487023985l);
        final String name = "Test1";
        final double temperature = 33.;

        //create device for SN
        createBeaconDevice(sn);

        final BeaconSignal b = new BeaconSignal();
        b.setSn(sn);
        b.setBattery(battery);
        b.setDistance(distance);
        b.setHardwareModel(hardwareModel);
        b.setHumidity(humidity);
        b.setLastScannedTime(lastScannedTime);
        b.setName(name);
        b.setTemperature(temperature);

        msg.getBeacons().add(b);
        msg.getBeacons().add(crateBeacon("7654321"));
        //create device for SN
        createBeaconDevice("7654321");

        process(msg);

        //test
        assertEquals(2, messages.size());

        final DeviceMessage m = messages.get(0);
        assertEquals(battery, Math.round(m.getBattery()));
        assertTrue(m.getImei().contains(sn));
        assertEquals(temperature, m.getTemperature(), 0.001);
        assertEquals((int) Math.round(humidity), m.getHumidity().intValue());
        assertTrue(Math.abs(lastScannedTime.getTime() - m.getTime().getTime()) < 1000l);
    }
    @Test
    public void testNotSendSystemMessageForNotFoundDevice() {
        process(createMessage("2304578320"));

        assertEquals(0, messages.size());
        assertEquals(0, alerts.size());
    }
    @Test
    public void testNotSendSystemMessageForNotActiveGateway() {
        final Bt04Message msg = createMessage(beacon.getImei());

        final GatewayBinding g = new GatewayBinding();
        g.setActive(false);
        g.setCompany(beacon.getCompany());
        g.setId(77l);
        g.setGateway("2304874320987209");
        beacon.setGateway(g);

        process(msg);

        assertEquals(0, messages.size());
        assertEquals(1, alerts.size());
    }
    @Test
    public void testNotSendSystemMessageForAnotherCompanyGateway() {
        final Bt04Message msg = createMessage(beacon.getImei());

        final GatewayBinding g = new GatewayBinding();
        g.setActive(true);
        g.setCompany(-111111l);
        g.setId(77l);
        g.setGateway("2304874320987209");
        beacon.setGateway(g);

        process(msg);

        assertEquals(0, messages.size());
        assertEquals(1, alerts.size());
    }
    @Test
    public void testNotSuccessToLockChannel() {
        final Bt04Message msg = createMessage(beacon.getImei());

        final GatewayBinding g = new GatewayBinding();
        g.setActive(true);
        g.setCompany(beacon.getCompany());
        g.setId(77l);
        g.setGateway(msg.getImei());
        beacon.setGateway(g);

        locks.add(beacon.getImei());
        process(msg);

        assertEquals(0, messages.size());
        assertEquals(0, alerts.size());
    }
    @Test
    public void testNotSendIfDeviceNotGateway() {
        final Bt04Message msg = createMessage(beacon.getImei());

        final GatewayBinding g = new GatewayBinding();
        g.setActive(true);
        g.setCompany(beacon.getCompany());
        g.setId(77l);
        g.setGateway("2304874320987209");
        beacon.setGateway(g);

        process(msg);

        assertEquals(0, messages.size());
        assertEquals(0, alerts.size());
    }
    @Test
    public void testAlertForInactiveDevice() {
        final Bt04Message m = createMessage(beacon.getImei());
        final Beacon d = createBeaconDevice(m.getBeacons().get(0).getSn());
        d.setActive(false);

        process(m);

        assertEquals(0, messages.size());
        assertEquals(1, alerts.size());
    }

    /* (non-Javadoc)
     * @see com.visfresh.bt04.Bt04Service#sendAlert(java.lang.String, java.lang.String)
     */
    @Override
    protected void sendAlert(final String subject, final String message) {
        alerts.add(subject + ":" + message);
    }
    /* (non-Javadoc)
     * @see com.visfresh.bt04.Bt04Service#sendMessage(com.visfresh.DeviceMessage, com.visfresh.Location)
     */
    @Override
    protected void sendMessage(final DeviceMessage msg) {
        msg.setId(lastId++);
        messages.add(msg);
    }
    /* (non-Javadoc)
     * @see com.visfresh.bt04.Bt04Service#getDeviceByImei(java.lang.String)
     */
    @Override
    protected Beacon getBeaconByImei(final String imei) {
        return beacons.get(imei);
    }
    /**
     * BT04 message
     */
    protected Bt04Message createMessage(final String beaconId) {
        final Bt04Message msg = new Bt04Message();
        msg.setImei("any-gateway");
        msg.setAccuracy(11.11);
        msg.setLatitude(22.22);
        msg.setLongitude(33.33);
        msg.setAltitude(44.44);
        msg.setTime(new Date(System.currentTimeMillis() - 10000000l));

        msg.getBeacons().add(crateBeacon(beaconId));

        return msg;
    }
    /**
     * @param sn
     * @return
     */
    private BeaconSignal crateBeacon(final String sn) {
        final BeaconSignal b = new BeaconSignal();
        b.setSn(sn);
        b.setBattery(50.);
        b.setDistance(100.);
        b.setHardwareModel("JUnit");
        b.setHumidity(33.);
        b.setLastScannedTime(new Date(System.currentTimeMillis() - 22342542354l));
        b.setName("JUnit");
        b.setTemperature(22.);
        return b;
    }
    /* (non-Javadoc)
     * @see au.smarttrace.bt04.Bt04Service#lockBeaconChannels(java.util.Set)
     */
    @Override
    protected Set<String> lockBeaconChannels(final Set<String> beacons, final String gateway) {
        final Set<String> result = new HashSet<>();
        for (final String b : beacons) {
            if (!locks.contains(b)) {
                result.add(b);
            }
        }
        return result;
    }
}
