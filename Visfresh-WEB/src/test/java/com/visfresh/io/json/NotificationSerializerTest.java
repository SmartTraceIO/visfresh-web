/**
 *
 */
package com.visfresh.io.json;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Date;

import org.junit.Before;
import org.junit.Test;

import com.google.gson.JsonObject;
import com.visfresh.entities.Alert;
import com.visfresh.entities.AlertType;
import com.visfresh.entities.Device;
import com.visfresh.entities.Notification;
import com.visfresh.entities.NotificationType;
import com.visfresh.entities.Shipment;
import com.visfresh.entities.TemperatureAlert;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class NotificationSerializerTest extends AbstractSerializerTest {
    private NotificationSerializer serializer = new NotificationSerializer(UTC);

    /**
     * Default constructor.
     */
    public NotificationSerializerTest() {
        super();
    }

    @Before
    public void setUp() {
        serializer.setDeviceResolver(resolver);
        serializer.setShipmentResolver(resolver);
    }
    @Test
    public void testEnterDarkEnvironmentAlertNotification() {
        final Date alertDate = new Date(System.currentTimeMillis() - 100000000l);
        final Long alertId = 77l;
        final Device device = createDevice("20394870987324");
        final Shipment shipment = createShipment();
        final AlertType alertType = AlertType.LightOff;

        Alert alert = new Alert();
        alert.setDate(alertDate);
        alert.setId(alertId);
        alert.setDevice(device);
        alert.setShipment(shipment);
        alert.setType(alertType);

        final Long notificationId = 78L;
        final NotificationType notificationType = NotificationType.Alert;

        Notification n= new Notification();
        n.setId(notificationId);
        n.setIssue(alert);
        n.setType(notificationType);

        final JsonObject json = serializer.toJson(n);
        n = serializer.parseNotification(json);

        //check notification
        assertEquals(notificationType, n.getType());
        assertEquals(notificationId, n.getId());

        //check issue
        alert = (Alert) n.getIssue();

        assertEquals(format(alertDate), format(alert.getDate()));
        assertEquals(alertId, alert.getId());
        assertNotNull(alert.getDevice());
        assertNotNull(alert.getShipment());
        assertEquals(alertType, alert.getType());
    }

    @Test
    public void testArrivalNotification() {
        final Date alertDate = new Date(System.currentTimeMillis() - 100000000l);
        final Long alertId = 77l;
        final Device device = createDevice("20394870987324");
        final Shipment shipment = createShipment();
        final AlertType alertType = AlertType.Hot;
        final double temperature = 10.12;
        final int minutes = 75;

        TemperatureAlert alert = new TemperatureAlert();
        alert.setDate(alertDate);
        alert.setId(alertId);
        alert.setDevice(device);
        alert.setType(alertType);
        alert.setTemperature(temperature);
        alert.setMinutes(minutes);
        alert.setShipment(shipment);

        final Long notificationId = 78L;
        final NotificationType notificationType = NotificationType.Alert;

        Notification n= new Notification();
        n.setId(notificationId);
        n.setIssue(alert);
        n.setType(notificationType);

        final JsonObject json = serializer.toJson(n);
        n = serializer.parseNotification(json);

        //check notification
        assertEquals(notificationType, n.getType());
        assertEquals(notificationId, n.getId());

        //check issue
        alert = (TemperatureAlert) n.getIssue();

        assertEquals(format(alertDate), format(alert.getDate()));
        assertEquals(alertId, alert.getId());
        assertNotNull(alert.getDevice());
        assertNotNull(alert.getShipment());
        assertEquals(alertType, alert.getType());
        assertEquals(temperature, alert.getTemperature(), 0.00001);
        assertEquals(minutes, alert.getMinutes());
    }
}
