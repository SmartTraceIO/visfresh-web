/**
 *
 */
package com.visfresh.io.json;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.text.ParseException;
import java.util.Date;

import org.junit.Before;
import org.junit.Test;

import com.google.gson.JsonObject;
import com.visfresh.entities.Color;
import com.visfresh.entities.Device;
import com.visfresh.entities.DeviceCommand;
import com.visfresh.entities.DeviceModel;
import com.visfresh.entities.Language;
import com.visfresh.entities.ListDeviceItem;
import com.visfresh.entities.ShipmentStatus;
import com.visfresh.entities.TemperatureUnits;
import com.visfresh.utils.DateTimeUtils;

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
        serializer = new DeviceSerializer(UTC, Language.English, TemperatureUnits.Celsius);
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
        final Color color = Color.Blue;
        final DeviceModel model = DeviceModel.TT18;

        t.setDescription(description);
        t.setImei(imei);
        t.setModel(model);
        t.setName(name);
        t.setActive(active);
        t.setAutostartTemplateId(autostartTemplateId);
        t.setColor(color);

        final JsonObject json = serializer.toJson(t).getAsJsonObject();
        t= serializer.parseDevice(json);

        assertEquals(description, t.getDescription());
        assertEquals(imei, t.getImei());
        assertEquals(model, t.getModel());
        assertEquals(name, t.getName());
        assertNotNull(t.getSn());
        assertEquals(active, t.isActive());
        assertEquals(autostartTemplateId, t.getAutostartTemplateId());
        assertEquals(color, t.getColor());
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
        ListDeviceItem dto = new ListDeviceItem();

        final boolean active = false;
        final Long autostartTemplateId = 754L;
        final String autostartTemplateName = "Autostart Template Name";
        final String description = "Device description";
        final Integer lastReadingBattery = 3000;
        final Double lastReadingLat = 56.78;
        final Double lastReadingLong = 12.34;
        final Double lastReadingTemperature = 12.245;
        final Date lastReadingTime = parseIso("2016-03-03 11:11:11");
        final Long lastShipmentId = 9l;
        final String name = "JUnit Device";
        final ShipmentStatus status = ShipmentStatus.Arrived;
        final Color color = Color.DarkOlivegreen;
        final DeviceModel model = DeviceModel.TT18;

        dto.setActive(active);
        dto.setAutostartTemplateId(autostartTemplateId);
        dto.setAutostartTemplateName(autostartTemplateName);
        dto.setDescription(description);
        dto.setBattery(lastReadingBattery);
        dto.setLatitude(lastReadingLat);
        dto.setLongitude(lastReadingLong);
        dto.setTemperature(lastReadingTemperature);
        dto.setLastReadingTime(lastReadingTime);
        dto.setShipmentId(lastShipmentId);
        dto.setName(name);
        dto.setModel(model);
        dto.setShipmentStatus(status);
        dto.setColor(color);

        dto = serializer.parseListDeviceItem(serializer.exportToView(dto));

        assertEquals(active, dto.isActive());
        assertEquals(autostartTemplateId, dto.getAutostartTemplateId());
        assertEquals(autostartTemplateName, dto.getAutostartTemplateName());
        assertEquals(description, dto.getDescription());
        assertEquals(lastReadingBattery, dto.getBattery());
        assertEquals(lastReadingLat, dto.getLatitude());
        assertEquals(lastReadingLong, dto.getLongitude());
        assertEquals(lastReadingTemperature, dto.getTemperature(), 0.1);
        assertEquals(lastReadingTime, dto.getLastReadingTime());
        assertEquals(lastShipmentId, dto.getShipmentId());
        assertEquals(name, dto.getName());
        assertEquals(status, dto.getShipmentStatus());
        assertEquals(color, dto.getColor());
        assertEquals(model, dto.getModel());
    }

    /**
     * @param str
     * @return
     */
    private Date parseIso(final String str) {
        if (str == null) {
            return null;
        }

        try {
            return DateTimeUtils.createIsoFormat(Language.English, UTC).parse(str);
        } catch (final ParseException e) {
            throw new RuntimeException(e);
        }
    }
}
