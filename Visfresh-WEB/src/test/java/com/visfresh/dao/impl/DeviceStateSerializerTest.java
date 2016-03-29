/**
 *
 */
package com.visfresh.dao.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.junit.Before;
import org.junit.Test;

import com.visfresh.entities.Location;
import com.visfresh.io.json.DeviceStateSerializer;
import com.visfresh.rules.state.ShipmentSession;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class DeviceStateSerializerTest {
    private DeviceStateSerializer serializer;

    /**
     * Default constructor.
     */
    public DeviceStateSerializerTest() {
        super();
    }

    /**
     * Initializes the test.
     */
    @Before
    public void setUp() {
        serializer = new DeviceStateSerializer();
    }
    @Test
    public void testSerialize() {
        ShipmentSession s = new ShipmentSession();
        final Date d1 = new Date(System.currentTimeMillis() - 100000);
        final Date d2 = new Date(System.currentTimeMillis() - 1000);
        final String key = "shipmentPropertyKey";
        final String value = "shipmentPropertyValue";
        final Location loc = new Location(10., 20.);

        s.getTemperatureAlerts().getDates().put("1", d1);
        s.getTemperatureAlerts().getDates().put("2", d2);
        s.getTemperatureAlerts().getProperties().put("key", "value");
        s.setShipmentProperty(key, value);
        s.setLastLocation(loc);

        final String str = serializer.toString(s);
        s = serializer.parseState(str);

        assertNotNull(s);

        assertEquals(format(d1), format(s.getTemperatureAlerts().getDates().get("1")));
        assertEquals(format(d2), format(s.getTemperatureAlerts().getDates().get("2")));
        assertEquals("value", s.getTemperatureAlerts().getProperties().get("key"));
        assertEquals(value, s.getShipmentProperty(key));
        assertEquals(loc.getLatitude(), s.getLastLocation().getLatitude(), 0.00001);
        assertEquals(loc.getLongitude(), s.getLastLocation().getLongitude(), 0.00001);
    }
    /**
     * @param date the date to format.
     * @return formatted date.
     */
    private String format(final Date date) {
        return new SimpleDateFormat("yyyy-MM-dd:HH:mm:ss").format(date);
    }
}
