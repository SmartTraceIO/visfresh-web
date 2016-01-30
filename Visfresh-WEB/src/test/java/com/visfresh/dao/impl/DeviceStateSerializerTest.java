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

import com.google.gson.JsonObject;
import com.visfresh.io.json.DeviceStateSerializer;
import com.visfresh.rules.state.DeviceState;
import com.visfresh.utils.SerializerUtils;

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
        final Long shipmentId = 77L;

        DeviceState s = new DeviceState();
        final Date d1 = new Date(System.currentTimeMillis() - 100000);
        final Date d2 = new Date(System.currentTimeMillis() - 1000);
        s.setShipmentId(shipmentId);

        s.getTemperatureAlerts().getDates().put("1", d1);
        s.getTemperatureAlerts().getDates().put("2", d2);
        s.getTemperatureAlerts().getProperties().put("key", "value");
        s.setArrivalProcessed(true);

        final String str = serializer.toString(s);
        s = serializer.parseState(str);

        assertNotNull(s);

        assertEquals(format(d1), format(s.getTemperatureAlerts().getDates().get("1")));
        assertEquals(format(d2), format(s.getTemperatureAlerts().getDates().get("2")));
        assertEquals("value", s.getTemperatureAlerts().getProperties().get("key"));
        assertEquals(shipmentId, s.getShipmentId());
        assertEquals(true, s.isArrivalProcessed());
    }
    @Test
    public void testSupportsNullArrivalProcessed() {
        DeviceState s = new DeviceState();
        s.setShipmentId(0l);

        final String str = serializer.toString(s);
        //remove arrival processed property
        final JsonObject obj = SerializerUtils.parseJson(str).getAsJsonObject();

        assertNotNull(obj.get("arrivalProcessed"));
        obj.remove("arrivalProcessed");

        s = serializer.parseState(obj.toString());

        assertNotNull(s);
        assertEquals(false, s.isArrivalProcessed());
    }
    /**
     * @param date the date to format.
     * @return formatted date.
     */
    private String format(final Date date) {
        return new SimpleDateFormat("yyyy-MM-dd:HH:mm:ss").format(date);
    }
}
