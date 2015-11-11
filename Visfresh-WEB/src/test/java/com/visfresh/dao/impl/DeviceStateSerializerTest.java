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

import com.visfresh.rules.DeviceState;

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
        DeviceState s = new DeviceState();
        final Date d1 = new Date(System.currentTimeMillis() - 100000);
        final Date d2 = new Date(System.currentTimeMillis() - 1000);

        s.getShipmentAutoStart().getDates().put("1", d1);
        s.getShipmentAutoStart().getDates().put("2", d2);
        s.getShipmentAutoStart().getProperties().put("key", "value");

        final String str = serializer.toString(s);
        s = serializer.parseState(str);

        assertNotNull(s);

        assertEquals(format(d1), format(s.getShipmentAutoStart().getDates().get("1")));
        assertEquals(format(d2), format(s.getShipmentAutoStart().getDates().get("2")));
        assertEquals("value", s.getShipmentAutoStart().getProperties().get("key"));
    }

    /**
     * @param d2
     * @return
     */
    private String format(final Date d2) {
        return new SimpleDateFormat("yyyy-MM-dd:HH:mm:ss").format(d2);
    }
}
