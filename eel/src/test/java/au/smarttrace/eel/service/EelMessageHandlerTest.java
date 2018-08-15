/**
 *
 */
package au.smarttrace.eel.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

import org.junit.Before;
import org.junit.Test;

import au.smarttrace.eel.Beacon;
import au.smarttrace.eel.GatewayBinding;
import au.smarttrace.eel.rawdata.BeaconData;
import au.smarttrace.eel.rawdata.DevicePosition;
import au.smarttrace.eel.rawdata.EelMessage;
import au.smarttrace.eel.rawdata.EelPackage;
import au.smarttrace.eel.rawdata.GpsData;
import au.smarttrace.eel.rawdata.GsmStationSignal;
import au.smarttrace.eel.rawdata.LocationPackageBody;
import au.smarttrace.eel.rawdata.PackageHeader;
import au.smarttrace.eel.rawdata.PackageHeader.PackageIdentifier;
import au.smarttrace.geolocation.DataWithGsmInfo;
import au.smarttrace.geolocation.DeviceMessage;
import au.smarttrace.geolocation.GeoLocationResponse;
import au.smarttrace.geolocation.Location;
import au.smarttrace.geolocation.MultiBeaconMessage;
import au.smarttrace.gsm.GsmLocationResolvingRequest;
import au.smarttrace.gsm.StationSignal;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class EelMessageHandlerTest extends EelMessageHandlerImpl {
    private Beacon beacon;
    private Map<String, Beacon> beacons = new HashMap<>();
    private final List<DeviceMessage> messages = new LinkedList<>();
    private final List<String> alerts = new LinkedList<>();
    private List<DataWithGsmInfo<MultiBeaconMessage>> locationRequests = new LinkedList<>();
    private Set<String> trackers = new HashSet<>();
    private final List<GeoLocationResponse> responses = new LinkedList<>();


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
    public void testProcessWithStationSignals() throws IOException {
        final Date time = new Date(System.currentTimeMillis() - 10000000l);

        final EelMessage msg = new EelMessage();
        msg.setImei(beacon.getImei());

        final EelPackage p = createLocationPackage();
        msg.getPackages().add(p);

        final DevicePosition pos = new DevicePosition();
        ((LocationPackageBody) p.getBody()).setLocation(pos);
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
        assertEquals(0, messages.size());
        assertEquals(1, this.locationRequests.size());

        final DataWithGsmInfo<MultiBeaconMessage> req = locationRequests.get(0);
        final MultiBeaconMessage msgs = req.getUserData();

        assertEquals(2, msgs.getBeacons().size());
        final DeviceMessage m = msgs.getBeacons().get(0);
        assertEquals(battery, Math.round(m.getBattery()));
        assertTrue(m.getImei().contains(address));
        assertEquals(temperature, m.getTemperature(), 0.001);
        assertTrue(Math.abs(time.getTime() - m.getTime().getTime()) < 2000l);

        assertNull(m.getLocation());

        final GsmLocationResolvingRequest gsmReq = req.getGsmInfo();
        assertEquals(2, gsmReq.getStations().size());
        final StationSignal ss = gsmReq.getStations().get(0);

        assertEquals(cid, ss.getCi());
        assertEquals(lac, ss.getLac());
        assertEquals(mcc, ss.getMcc());
        assertEquals(mnc, ss.getMnc());
        assertEquals(rxLevel, ss.getLevel());
    }
    @Test
    public void testIncludeTracker() throws IOException {
        final String imei = "123456677";
        final EelMessage msg = crtateLocationMessage(imei, null, createGsmStationSignal());
        //add one beacon
        final LocationPackageBody lp = getLocationPackage(msg, 0);

        final BeaconData beaconData = createBeaconData("9038749879");
        lp.getBeacons().add(beaconData);

        handleMessage(msg);

        //test
        assertEquals(0, messages.size());

        //test not registered device
        assertEquals(0, this.locationRequests.size());

        //register device
        createBeaconDevice(beaconData.getAddress());
        handleMessage(msg);

        assertEquals(0, messages.size());
        assertEquals(1, this.locationRequests.size());

        final DataWithGsmInfo<MultiBeaconMessage> req = locationRequests.get(0);
        final MultiBeaconMessage msgs = req.getUserData();

        assertEquals(1, msgs.getBeacons().size());
        final DeviceMessage m = msgs.getBeacons().get(0);
        assertEquals(beaconData.getAddress(), m.getImei());
    }
    @Test
    public void testIncludeTrackerResolvedLocation() throws IOException {
        final String imei = "123456677";
        final EelMessage msg = crtateLocationMessage(imei, new Location(), null);

        handleMessage(msg);

        //test
        assertEquals(0, messages.size());

        //register device
        trackers.add(imei);
        handleMessage(msg);

        assertEquals(1, messages.size());
        final DeviceMessage m = messages.get(0);
        assertEquals(imei, m.getImei());
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
    /* (non-Javadoc)
     * @see au.smarttrace.eel.service.EelMessageHandlerImpl#processResolvedMessage(au.smarttrace.geolocation.MultiBeaconMessage)
     */
    @Override
    protected void processResolvedMessage(final MultiBeaconMessage req) {
        if (req.getGatewayMessage() != null) {
            messages.add(req.getGatewayMessage());
        }
        messages.addAll(req.getBeacons());
    }
    /**
     * @param req
     */
    @Override
    protected void sendLocationResolvingRequest(final DataWithGsmInfo<MultiBeaconMessage> req) {
        final MultiBeaconMessage userData = req.getUserData();
        if (userData.getGatewayMessage() != null || userData.getBeacons().size() > 0) {
            this.locationRequests.add(req);
        }
    }
    /* (non-Javadoc)
     * @see au.smarttrace.eel.service.EelMessageHandlerImpl#getAndRemoveProcessedResponses(java.lang.String, int)
     */
    @Override
    protected List<GeoLocationResponse> getAndRemoveProcessedResponses(final String sender, final int limit) {
        final List<GeoLocationResponse> result = new LinkedList<>();

        final Iterator<GeoLocationResponse> iter = responses.iterator();
        while (iter.hasNext()) {
            if (result.size() >= limit) {
                break;
            }

            final GeoLocationResponse next = iter.next();
            if (next.getStatus() != null) {
                result.add(next);
                iter.remove();
            }
        }

        return result;
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
     * @see com.visfresh.bt04.Bt04Service#getDeviceByImei(java.lang.String)
     */
    @Override
    protected Beacon getBeaconByImei(final String imei) {
        return beacons.get(imei);
    }
    /* (non-Javadoc)
     * @see au.smarttrace.eel.service.EelMessageHandlerImpl#isTrackerRegistered(java.lang.String)
     */
    @Override
    protected boolean isTrackerRegistered(final String imei) {
        return trackers.contains(imei);
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
    /**
     * @param imei
     * @param loc
     * @param createGsmStationSignal
     * @return
     */
    private EelMessage crtateLocationMessage(final String imei, final Location loc,
            final GsmStationSignal sig) {
        final Date time = new Date(System.currentTimeMillis() - 10000000l);

        final EelMessage msg = new EelMessage();
        msg.setImei(imei);

        final EelPackage p = createLocationPackage();
        msg.getPackages().add(p);

        final DevicePosition pos = new DevicePosition();
        ((LocationPackageBody) p.getBody()).setLocation(pos);
        pos.setTime(toEpoch(time.getTime()));

        if (loc != null) {
            final GpsData gps = new GpsData();
            gps.setLatitude((int) Math.round(loc.getLatitude() * COORDINATES_MASHTAB));
            gps.setLongitude((int) Math.round(loc.getLongitude() * COORDINATES_MASHTAB));

            pos.setGpsData(gps);
        }
        if(sig != null) {
            pos.getTowerSignals().add(sig);
        }

        return msg;
    }
    /**
     * @return
     */
    private GsmStationSignal createGsmStationSignal() {
        final GsmStationSignal sig = new GsmStationSignal();
        sig.setCid(1111);
        sig.setLac(222);
        sig.setMcc(333);
        sig.setMnc(444);
        sig.setRxLevel(55);

        return sig;
    }
    /**
     * @return
     */
    protected EelPackage createLocationPackage() {
        final EelPackage p = new EelPackage();

        //header
        final PackageHeader h = new PackageHeader();
        h.setPid(PackageIdentifier.Location);
        p.setHeader(h);

        //body
        final LocationPackageBody body = new LocationPackageBody();
        p.setBody(body);

        return p;
    }
    /**
     * @param t
     * @return
     */
    private long toEpoch(final long t) {
        return (t + TimeZone.getDefault().getOffset(t)) / 1000;
    }

    /**
     * @param imei
     * @return
     */
    private BeaconData createBeaconData(final String imei) {
        final BeaconData bd = new BeaconData();
        bd.setAddress(imei);
        return bd;
    }
    /**
     * @param msg
     * @return
     */
    private LocationPackageBody getLocationPackage(final EelMessage msg, final int pos) {
        final EelPackage pckg = msg.getPackages().get(pos);
        return (LocationPackageBody) pckg.getBody();
    }
}
