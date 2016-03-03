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
import com.visfresh.entities.ShipmentStatus;
import com.visfresh.lists.ListDeviceItemDto;

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
        final Long autostartTemplateId = 777l;

        t.setDescription(description);
        t.setImei(imei);
        t.setName(name);
        t.setActive(active);
        t.setAutostartTemplateId(autostartTemplateId);

        final JsonObject json = serializer.toJson(t).getAsJsonObject();
        t= serializer.parseDevice(json);

        assertEquals(description, t.getDescription());
        assertEquals(imei, t.getImei());
        assertEquals(name, t.getName());
        assertNotNull(t.getSn());
        assertEquals(active, t.isActive());
        assertEquals(autostartTemplateId, t.getAutostartTemplateId());
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
    @Test
    public void testListDeviceItemDto() {
        ListDeviceItemDto dto = new ListDeviceItemDto();

        final boolean active = false;
        final Long autostartTemplateId = 754L;
        final String autostartTemplateName = "Autostart Template Name";
        final String description = "Device description";
        final Integer lastReadingBattery = 3000;
        final Double lastReadingLat = 56.78;
        final Double lastReadingLong = 12.34;
        final String lastReadingTemperature = "12.245";
        final String lastReadingTimeISO = "2016-03-03 11:11:11";
        final Long lastShipmentId = 9l;
        final String name = "JUnit Device";
        final String shipmentNumber = "12345(10)";
        final String status = ShipmentStatus.Arrived.name();
        final String sn = "12345";

        dto.setActive(active);
        dto.setAutostartTemplateId(autostartTemplateId);
        dto.setAutostartTemplateName(autostartTemplateName);
        dto.setDescription(description);
        dto.setLastReadingBattery(lastReadingBattery);
        dto.setLastReadingLat(lastReadingLat);
        dto.setLastReadingLong(lastReadingLong);
        dto.setLastReadingTemperature(lastReadingTemperature);
        dto.setLastReadingTimeISO(lastReadingTimeISO);
        dto.setLastShipmentId(lastShipmentId);
        dto.setName(name);
        dto.setShipmentNumber(shipmentNumber);
        dto.setShipmentStatus(status);
        dto.setSn(sn);

        dto = serializer.parseListDeviceItem(serializer.toJson(dto));

        assertEquals(active, dto.isActive());
        assertEquals(autostartTemplateId, dto.getAutostartTemplateId());
        assertEquals(autostartTemplateName, dto.getAutostartTemplateName());
        assertEquals(description, dto.getDescription());
        assertEquals(lastReadingBattery, dto.getLastReadingBattery());
        assertEquals(lastReadingLat, dto.getLastReadingLat());
        assertEquals(lastReadingLong, dto.getLastReadingLong());
        assertEquals(lastReadingTemperature, dto.getLastReadingTemperature());
        assertEquals(lastReadingTimeISO, dto.getLastReadingTimeISO());
        assertEquals(lastShipmentId, dto.getLastShipmentId());
        assertEquals(name, dto.getName());
        assertEquals(shipmentNumber, dto.getShipmentNumber());
        assertEquals(status, dto.getShipmentStatus());
        assertEquals(sn, dto.getSn());
    }
}
