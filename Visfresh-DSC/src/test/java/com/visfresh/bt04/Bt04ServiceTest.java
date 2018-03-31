/**
 *
 */
package com.visfresh.bt04;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.visfresh.Device;
import com.visfresh.DeviceMessage;
import com.visfresh.DeviceMessageType;
import com.visfresh.Location;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class Bt04ServiceTest extends Bt04Service {
    private Device device;
    private Map<String, Device> devices = new HashMap<>();
    private long lastId;
    private final List<DeviceMessage> messages = new LinkedList<>();
    private final List<String> alerts = new LinkedList<>();
    private Map<Long, Location> locations = new HashMap<>();

    /**
     * Default constructor.
     */
    public Bt04ServiceTest() {
        super();
    }
    @Before
    public void setUp() {
        this.device = addDevice("1289374698773");
    }

    /**
     * @param imei
     */
    private Device addDevice(final String imei) {
        final Device d = new Device();
        d.setImei(imei);
        d.setName("JUnit-" + imei);
        d.setDescription("Test device");
        d.setActive(true);

        devices.put(d.getImei(), d);
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
        msg.setImei(device.getImei());
        msg.setAccuracy(accuracy);
        msg.setLatitude(latitude);
        msg.setLongitude(longitude);
        msg.setAltitude(altitude);
        msg.setTime(time);

        //beacons
        final String sn = "beacon-ID";
        final double battery = 50.;
        final double distance = 100.;
        final String hardwareModel = "M123";
        final double humidity = 45.;
        final Date lastScannedTime = new Date(System.currentTimeMillis() - 239487023985l);
        final String name = "Test1";
        final double temperature = 33.;

        //create device for SN
        addDevice(sn);

        final Beacon b = new Beacon();
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
        addDevice("7654321");

        process(msg);

        //test
        assertEquals(2, messages.size());

        final DeviceMessage m = messages.get(0);
        assertEquals(Bt04Service.BATTERY_FULL / 100. * battery, m.getBattery(), 0.001);
        assertNull(m.getBeaconId());
        assertTrue(m.getImei().contains(sn));
        assertEquals(temperature, m.getTemperature(), 0.001);
        assertTrue(Math.abs(lastScannedTime.getTime() - m.getTime().getTime()) < 1000l);
        assertEquals(DeviceMessageType.AUT, m.getType());
    }
    @Test
    public void testNotSendSystemMessageForNotFoundDevice() {
        process(createMessage("2304578320"));

        assertEquals(0, messages.size());
        assertEquals(0, alerts.size());
    }
    @Test
    public void testAlertForInactiveDevice() {
        final Bt04Message m = createMessage(device.getImei());
        final Device d = addDevice(m.getBeacons().get(0).getSn());
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
    protected void sendMessage(final DeviceMessage msg, final Location loc) {
        msg.setId(lastId++);
        messages.add(msg);
        locations.put(msg.getId(), loc);
    }
    /* (non-Javadoc)
     * @see com.visfresh.bt04.Bt04Service#getDeviceByImei(java.lang.String)
     */
    @Override
    protected Device getDeviceByImei(final String imei) {
        return devices.get(imei);
    }
    /**
     * BT04 message
     */
    protected Bt04Message createMessage(final String imei) {
        final Bt04Message msg = new Bt04Message();
        msg.setImei(imei);
        msg.setAccuracy(11.11);
        msg.setLatitude(22.22);
        msg.setLongitude(33.33);
        msg.setAltitude(44.44);
        msg.setTime(new Date(System.currentTimeMillis() - 10000000l));

        msg.getBeacons().add(crateBeacon("76432"));
        return msg;
    }
    /**
     * @param sn
     * @return
     */
    private Beacon crateBeacon(final String sn) {
        final Beacon b = new Beacon();
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
}
