/**
 *
 */
package com.visfresh.io.json;

import static org.junit.Assert.assertEquals;

import java.util.Date;

import org.junit.Test;

import com.google.gson.JsonElement;
import com.visfresh.entities.Location;
import com.visfresh.impl.services.DeviceDcsNativeEvent;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class DeviceDcsNativeEventSerializerTest extends AbstractSerializerTest {
    private final DeviceDcsNativeEventSerializer serializer = new DeviceDcsNativeEventSerializer();

    /**
     * Default constructor.
     */
    public DeviceDcsNativeEventSerializerTest() {
        super();
    }

    @Test
    public void testDeviceDcsNativeEvent() {
        DeviceDcsNativeEvent e = new DeviceDcsNativeEvent();

        final int battery = 14;
        final Date date = new Date(System.currentTimeMillis() - 100000l);
        final Date createdOn = new Date(System.currentTimeMillis() - 200000l);
        final String imei = "923847924387";
        final double lat = 100.500;
        final double lon = 100.501;
        final String type = "Tracker";
        final String gateway = "beacon-gateway";

        e.setBattery(battery);
        e.setDate(date);
        e.setCreatedOn(createdOn);
        e.setImei(imei);
        e.setLocation(new Location(lat, lon));
        e.setType(type);
        e.setGateway(gateway);

        final JsonElement json = serializer.toJson(e);
        e = serializer.parseDeviceDcsNativeEvent(json);

        assertEquals(battery, e.getBattery());
        assertEquals(format(date), format(e.getDate()));
        assertEquals(format(createdOn), format(e.getCreatedOn()));
        assertEquals(imei, e.getImei());
        assertEquals(lat, e.getLocation().getLatitude(), 0.00001);
        assertEquals(lon, e.getLocation().getLongitude(), 0.00001);
        assertEquals(type, e.getType());
        assertEquals(gateway, e.getGateway());
    }
}
