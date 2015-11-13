/**
 *
 */
package com.visfresh.io.json;

import static org.junit.Assert.assertEquals;

import java.util.Date;

import org.junit.Test;

import com.google.gson.JsonElement;
import com.visfresh.mpl.services.DeviceDcsNativeEvent;

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
        final String imei = "923847924387";
        final double lat = 100.500;
        final double lon = 100.501;
        final String type = "Tracker";

        e.setBattery(battery);
        e.setDate(date);
        e.setImei(imei);
        e.getLocation().setLatitude(lat);
        e.getLocation().setLongitude(lon);
        e.setType(type);

        final JsonElement json = serializer.toJson(e);
        e = serializer.parseDeviceDcsNativeEvent(json);

        assertEquals(battery, e.getBattery());
        assertEquals(format(date), format(e.getTime()));
        assertEquals(imei, e.getImei());
        assertEquals(lat, e.getLocation().getLatitude(), 0.00001);
        assertEquals(lon, e.getLocation().getLongitude(), 0.00001);
        assertEquals(type, e.getType());
    }
}
