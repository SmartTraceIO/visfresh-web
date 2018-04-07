/**
 *
 */
package com.visfresh.dao.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Before;
import org.junit.Test;

import com.visfresh.io.json.DeviceStateSerializer;
import com.visfresh.rules.state.DeviceState;

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
        s.setProperty("abra", "kadabra");

        final String str = serializer.toString(s);
        s = serializer.parseState(str);

        assertNotNull(s);

        assertEquals("kadabra", s.getProperty("abra"));
    }
    @Test
    public void testSerializeWithBeaconID() {
        DeviceState s = new DeviceState();
        s.setProperty("abra", "kadabra");

        final String str = serializer.toString(s);
        s = serializer.parseState(str);

        assertNotNull(s);

        assertEquals("kadabra", s.getProperty("abra"));
    }
}
