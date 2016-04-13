/**
 *
 */
package com.visfresh.io.json;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import com.google.gson.JsonObject;
import com.visfresh.entities.DeviceGroup;
import com.visfresh.entities.User;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class DeviceGroupSerializerTest {
    /**
     * Device group serializer.
     */
    private DeviceGroupSerializer serializer;

    /**
     * Default constructor.
     */
    public DeviceGroupSerializerTest() {
        super();
    }
    @Before
    public void setUp() {
        final User user = new User();
        serializer = new DeviceGroupSerializer(user);
    }

    /**
     * Test device group serialization.
     */
    @Test
    public void testDeviceGroup() {
        final String description = "Device group description";
        final String name = "Device Group Name";
        final Long id = 777l;

        DeviceGroup group = new DeviceGroup();
        group.setDescription(description);
        group.setName(name);
        group.setId(id);

        final JsonObject json = serializer.toJson(group);
        group = serializer.parseDeviceGroup(json);

        assertEquals(name, group.getName());
        assertEquals(description, group.getDescription());
        assertEquals(id, group.getId());
    }
}
