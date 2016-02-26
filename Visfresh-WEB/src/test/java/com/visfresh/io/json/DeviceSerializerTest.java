/**
 *
 */
package com.visfresh.io.json;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Before;
import org.junit.Test;

import com.google.gson.JsonObject;
import com.visfresh.entities.Device;
import com.visfresh.entities.DeviceCommand;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class DeviceSerializerTest extends AbstractSerializerTest {
    private DeviceSerializer serializer;
    /**
     * Default constructor.
     */
    public DeviceSerializerTest() {
        super();
    }

    @Before
    public void setUp() {
        serializer = new DeviceSerializer(UTC);
        serializer.setDeviceResolver(resolver);
    }
    @Test
    public void testDevice() {
        Device t = new Device();

        final String description = "Device description";
        final String imei = "018923475076";
        final String name = "Device Name";
        final boolean active = !t.isActive();

        t.setDescription(description);
        t.setImei(imei);
        t.setName(name);
        t.setActive(active);

        final JsonObject json = serializer.toJson(t).getAsJsonObject();
        t= serializer.parseDevice(json);

        assertEquals(description, t.getDescription());
        assertEquals(imei, t.getImei());
        assertEquals(name, t.getName());
        assertNotNull(t.getSn());
        assertEquals(active, t.isActive());
    }
    @Test
    public void testDeviceCommand() {
        final Device device = createDevice("2380947093287");
        final String command = "shutdown";

        DeviceCommand cmd = new DeviceCommand();
        cmd.setDevice(device);
        cmd.setCommand(command);

        final JsonObject obj = serializer.toJson(cmd).getAsJsonObject();
        cmd = serializer.parseDeviceCommand(obj);

        assertEquals(command, cmd.getCommand());
        assertNotNull(cmd.getDevice());
    }
}
