/**
 *
 */
package au.smarttrace.eel.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

import org.junit.Before;
import org.junit.Test;

import au.smarttrace.eel.Beacon;
import au.smarttrace.eel.DeviceMessage;
import au.smarttrace.eel.GatewayBinding;
import au.smarttrace.eel.Location;
import au.smarttrace.eel.StationSignal;
import au.smarttrace.eel.rawdata.BeaconData;
import au.smarttrace.eel.rawdata.DevicePosition;
import au.smarttrace.eel.rawdata.EelMessage;
import au.smarttrace.eel.rawdata.EelPackage;
import au.smarttrace.eel.rawdata.GpsData;
import au.smarttrace.eel.rawdata.GsmStationSignal;
import au.smarttrace.eel.rawdata.LocationPackageBody;
import au.smarttrace.eel.rawdata.PackageHeader;
import au.smarttrace.eel.rawdata.PackageHeader.PackageIdentifier;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class EelMessageHandlerTest extends EelMessageHandlerImpl {
    private Beacon beacon;
    private Map<String, Beacon> beacons = new HashMap<>();
    private long lastId;
    private final List<DeviceMessage> messages = new LinkedList<>();
    private final List<String> alerts = new LinkedList<>();
    private final Set<String> locks = new HashSet<>();
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
    public void testProcessWithGpsData() {
        final double latitude = 22.22;
        final double longitude = 33.33;
        final long altitude = 444;
        final Date time = new Date(System.currentTimeMillis() - 10000000l);

        final EelMessage msg = new EelMessage();
        msg.setImei(beacon.getImei());

        final EelPackage p = new EelPackage();
        msg.getPackages().add(p);

        final PackageHeader h = new PackageHeader();
        h.setPid(PackageIdentifier.Location);
        p.setHeader(h);

        final LocationPackageBody body = new LocationPackageBody();
        p.setBody(body);

        final DevicePosition pos = new DevicePosition();
        body.setLocation(pos);
        pos.setTime(toEpoch(time.getTime()));

        final GpsData gps = new GpsData();
        gps.setLatitude((long) (latitude * COORDINATES_MASHTAB));
        gps.setLongitude((long) (longitude * COORDINATES_MASHTAB));
        gps.setAltitude(altitude);
        pos.setGpsData(gps);

        //beacons
        final String address = "beacon-ID";
        final int battery = 50;
        final double temperature = 33.;
        final int rssi = 444;

        //create device for SN
        createBeaconDevice(address);

        final BeaconData b = new BeaconData();
        b.setAddress(address);
        b.setBattery(battery);
        b.setTemperature((int) Math.round(temperature * 256));
        b.setRssi(rssi);

        getBeacons(msg).add(b);
        getBeacons(msg).add(crateBeacon("7654321"));
        //create device for SN
        createBeaconDevice("7654321");

        handleMessage(msg);

        //test
        assertEquals(2, messages.size());

        final DeviceMessage m = messages.get(0);
        assertEquals(battery, Math.round(m.getBattery()));
        assertTrue(m.getImei().contains(address));
        assertEquals(temperature, m.getTemperature(), 0.001);
        assertTrue(Math.abs(time.getTime() - m.getTime().getTime()) < 2000l);

        final Location loc = m.getLocation();
        assertNotNull(loc);
        assertEquals(latitude, loc.getLatitude(), 0.01);
        assertEquals(longitude, loc.getLongitude(), 0.01);
    }
    @Test
    public void testProcessWithStationSignals() {
        final Date time = new Date(System.currentTimeMillis() - 10000000l);

        final EelMessage msg = new EelMessage();
        msg.setImei(beacon.getImei());

        final EelPackage p = new EelPackage();
        msg.getPackages().add(p);

        final PackageHeader h = new PackageHeader();
        h.setPid(PackageIdentifier.Location);
        p.setHeader(h);

        final LocationPackageBody body = new LocationPackageBody();
        p.setBody(body);

        final DevicePosition pos = new DevicePosition();
        body.setLocation(pos);
        pos.setTime(toEpoch(time.getTime()));

        final GsmStationSignal sig = new GsmStationSignal();
        pos.getTowerSignals().add(sig);
        pos.getTowerSignals().add(sig);

        final int cid = 1111;
        final int lac = 222;
        final int mcc = 333;
        final int mnc = 444;
        final int rxLevel = 555;

        sig.setCid(cid);
        sig.setLac(lac);
        sig.setMcc(mcc);
        sig.setMnc(mnc);
        sig.setRxLevel(rxLevel);

        //beacons
        final String address = "beacon-ID";
        final int battery = 50;
        final double temperature = 33.;
        final int rssi = 444;

        //create device for SN
        createBeaconDevice(address);

        final BeaconData b = new BeaconData();
        b.setAddress(address);
        b.setBattery(battery);
        b.setTemperature((int) Math.round(temperature * 256));
        b.setRssi(rssi);

        getBeacons(msg).add(b);
        getBeacons(msg).add(crateBeacon("7654321"));
        //create device for SN
        createBeaconDevice("7654321");

        handleMessage(msg);

        //test
        assertEquals(2, messages.size());

        final DeviceMessage m = messages.get(0);
        assertEquals(battery, Math.round(m.getBattery()));
        assertTrue(m.getImei().contains(address));
        assertEquals(temperature, m.getTemperature(), 0.001);
        assertTrue(Math.abs(time.getTime() - m.getTime().getTime()) < 2000l);

        assertNull(m.getLocation());

        assertEquals(2, m.getStationSignals().size());
        final StationSignal ss = m.getStationSignals().get(0);

        assertEquals(cid, ss.getCi());
        assertEquals(lac, ss.getLac());
        assertEquals(mcc, ss.getMcc());
        assertEquals(mnc, ss.getMnc());
        assertEquals(rxLevel, ss.getLevel());
    }
    /**
     * @param t
     * @return
     */
    private long toEpoch(final long t) {
        return (t + TimeZone.getDefault().getOffset(t)) / 1000;
    }

    @Test
    public void testNotSendSystemMessageForNotFoundDevice() {
        handleMessage(createMessage("2304578320"));

        assertEquals(0, messages.size());
        assertEquals(0, alerts.size());
    }
    @Test
    public void testNotSendSystemMessageForNotActiveGateway() {
        final EelMessage msg = createMessage(beacon.getImei());

        final GatewayBinding g = new GatewayBinding();
        g.setActive(false);
        g.setCompany(beacon.getCompany());
        g.setId(77l);
        g.setGateway("2304874320987209");
        beacon.setGateway(g);

        handleMessage(msg);

        assertEquals(0, messages.size());
        assertEquals(1, alerts.size());
    }
    @Test
    public void testNotSendSystemMessageForAnotherCompanyGateway() {
        final EelMessage msg = createMessage(beacon.getImei());

        final GatewayBinding g = new GatewayBinding();
        g.setActive(true);
        g.setCompany(-111111l);
        g.setId(77l);
        g.setGateway("2304874320987209");
        beacon.setGateway(g);

        handleMessage(msg);

        assertEquals(0, messages.size());
        assertEquals(1, alerts.size());
    }
    @Test
    public void testNotSuccessToLockChannel() {
        final EelMessage msg = createMessage(beacon.getImei());

        final GatewayBinding g = new GatewayBinding();
        g.setActive(true);
        g.setCompany(beacon.getCompany());
        g.setId(77l);
        g.setGateway(msg.getImei());
        beacon.setGateway(g);

        locks.add(beacon.getImei());
        handleMessage(msg);

        assertEquals(0, messages.size());
        assertEquals(0, alerts.size());
    }
    @Test
    public void testNotSendIfDeviceNotGateway() {
        final EelMessage msg = createMessage(beacon.getImei());

        final GatewayBinding g = new GatewayBinding();
        g.setActive(true);
        g.setCompany(beacon.getCompany());
        g.setId(77l);
        g.setGateway("2304874320987209");
        beacon.setGateway(g);

        handleMessage(msg);

        assertEquals(0, messages.size());
        assertEquals(0, alerts.size());
    }
    @Test
    public void testAlertForInactiveDevice() {
        final EelMessage m = createMessage(beacon.getImei());
        final Beacon d = createBeaconDevice(getBeacons(m).get(0).getAddress());
        d.setActive(false);

        handleMessage(m);

        assertEquals(0, messages.size());
        assertEquals(1, alerts.size());
    }

    /**
     * @param m
     * @return
     */
    private List<BeaconData> getBeacons(final EelMessage m) {
        return ((LocationPackageBody) m.getPackages().get(0).getBody()).getBeacons();
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
    protected EelMessage createMessage(final String beaconId) {
        final EelMessage msg = new EelMessage();
        msg.setMark("6767");
        msg.setImei("any-gateway");
        msg.setRawData("Fake raw data".getBytes());

        final EelPackage p = new EelPackage();
        msg.getPackages().add(p);
        final PackageHeader header = new PackageHeader();
        header.setMark("6767");
        header.setPid(PackageIdentifier.Location);
        p.setHeader(header);

        final LocationPackageBody body = new LocationPackageBody();
        body.setBattery(5555);
        body.setBeaconVersion(1);
        body.setTemperature(22 * 256);
        p.setBody(body);

        final DevicePosition pos = new DevicePosition();

//        GpsData
        final GpsData gps = new GpsData();
        pos.setGpsData(gps);
        gps.setLatitude((long) (22.22 * COORDINATES_MASHTAB));
        gps.setLongitude((long) (33.33 * COORDINATES_MASHTAB));
        gps.setAltitude(1500);
        pos.setTime((System.currentTimeMillis() - 10000000l) / 1000);

        body.setLocation(pos);
        body.getBeacons().add(crateBeacon(beaconId));

        return msg;
    }
    /**
     * @param address
     * @return
     */
    private BeaconData crateBeacon(final String address) {
        final BeaconData b = new BeaconData();
        b.setAddress(address);
        b.setBattery(3700);
        b.setRssi(555);
        b.setTppe((byte) 222);
        b.setTemperature(22 * 256);
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
