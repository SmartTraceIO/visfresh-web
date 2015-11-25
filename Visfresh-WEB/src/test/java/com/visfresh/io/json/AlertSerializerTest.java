/**
 *
 */
package com.visfresh.io.json;

import static org.junit.Assert.assertEquals;

import java.util.Date;

import org.junit.Before;
import org.junit.Test;

import com.google.gson.JsonElement;
import com.visfresh.entities.Alert;
import com.visfresh.entities.AlertType;
import com.visfresh.entities.Device;
import com.visfresh.entities.Shipment;
import com.visfresh.entities.TemperatureAlert;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class AlertSerializerTest extends AbstractSerializerTest {
    private AlertSerializer serializer = new AlertSerializer(UTC);

    /**
     * Default constructor.
     */
    public AlertSerializerTest() {
        super();
    }

    @Before
    public void setUp() {
        serializer.setDeviceResolver(resolver);
        serializer.setShipmentResolver(resolver);
    }
    @Test
    public void testAlert() {
        final Device device = createDevice("92348072043987");
        final Shipment shipment = createShipment();
        final Date date = new Date(System.currentTimeMillis() - 100000000l);
        final Long id = generateId();
        final AlertType type = AlertType.Battery;

        Alert alert = new Alert();
        alert.setDate(date);
        alert.setId(id);
        alert.setDevice(device);
        alert.setType(type);
        alert.setShipment(shipment);

        final JsonElement json = serializer.toJson(alert);
        alert = serializer.parseAlert(json);

        assertEquals(format(date), format(alert.getDate()));
        assertEquals(device.getId(), alert.getDevice().getId());
        assertEquals(id, alert.getId());
        assertEquals(shipment.getId(), alert.getShipment().getId());
        assertEquals(type, alert.getType());
    }
    @Test
    public void testTemparatureAlert() {
        final Device device = createDevice("92348072043987");
        final Shipment shipment = createShipment();
        final Date date = new Date(System.currentTimeMillis() - 100000000l);
        final Long id = generateId();
        final AlertType type = AlertType.CriticalHot;
        final double temperature = -20.3;
        final int minutes = 30;
        final boolean cumulative = true;

        TemperatureAlert alert = new TemperatureAlert();
        alert.setDate(date);
        alert.setId(id);
        alert.setDevice(device);
        alert.setType(type);
        alert.setShipment(shipment);
        alert.setTemperature(temperature);
        alert.setMinutes(minutes);
        alert.setCumulative(cumulative);

        final JsonElement json = serializer.toJson(alert);
        alert = (TemperatureAlert) serializer.parseAlert(json);

        assertEquals(format(date), format(alert.getDate()));
        assertEquals(device.getId(), alert.getDevice().getId());
        assertEquals(id, alert.getId());
        assertEquals(shipment.getId(), alert.getShipment().getId());
        assertEquals(type, alert.getType());
        assertEquals(temperature, alert.getTemperature(), 0.00001);
        assertEquals(minutes, alert.getMinutes());
        assertEquals(cumulative, alert.isCumulative());
    }
}
