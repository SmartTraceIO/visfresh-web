/**
 *
 */
package com.visfresh.dao.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.junit.Before;
import org.junit.Test;

import com.visfresh.io.json.ShipmentSessionSerializer;
import com.visfresh.rules.state.ShipmentSession;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class ShipmentSessionSerializerTest {
    private ShipmentSessionSerializer serializer;

    /**
     * Default constructor.
     */
    public ShipmentSessionSerializerTest() {
        super();
    }

    /**
     * Initializes the test.
     */
    @Before
    public void setUp() {
        serializer = new ShipmentSessionSerializer();
    }
    @Test
    public void testSerialize() {
        ShipmentSession s = new ShipmentSession();
        final Date d1 = new Date(System.currentTimeMillis() - 100000);
        final Date d2 = new Date(System.currentTimeMillis() - 1000);
        final Date alertsSupressionDate = new Date(System.currentTimeMillis() - 209832470932l);
        final String key = "shipmentPropertyKey";
        final String value = "shipmentPropertyValue";

        s.getTemperatureAlerts().getDates().put("1", d1);
        s.getTemperatureAlerts().getDates().put("2", d2);
        s.getTemperatureAlerts().getProperties().put("key", "value");
        s.setShipmentProperty(key, value);
        s.setAlertsSuppressed(true);
        s.setAlertsSuppressionDate(alertsSupressionDate);
        s.setBatteryLowProcessed(true);

        final String str = serializer.toString(s);
        s = serializer.parseSession(str);

        assertNotNull(s);

        assertEquals(format(d1), format(s.getTemperatureAlerts().getDates().get("1")));
        assertEquals(format(d2), format(s.getTemperatureAlerts().getDates().get("2")));
        assertEquals("value", s.getTemperatureAlerts().getProperties().get("key"));
        assertEquals(value, s.getShipmentProperty(key));
        assertTrue(s.isAlertsSuppressed());
        assertTrue(s.isBatteryLowProcessed());
        assertEquals(format(alertsSupressionDate), format(s.getAlertsSuppressionDate()));
    }
    @Test
    public void testEmptySession() {
        final ShipmentSession s = serializer.parseSession("{}");
        assertNotNull(s);
    }
    /**
     * @param date the date to format.
     * @return formatted date.
     */
    private String format(final Date date) {
        return new SimpleDateFormat("yyyy-MM-dd:HH:mm:ss").format(date);
    }
}
