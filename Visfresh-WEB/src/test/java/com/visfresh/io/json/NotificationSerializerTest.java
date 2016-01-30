/**
 *
 */
package com.visfresh.io.json;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import com.google.gson.JsonObject;
import com.visfresh.entities.AlertType;
import com.visfresh.entities.NotificationType;
import com.visfresh.io.NotificationItem;

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
    }
    @Test
    public void testAlertNotification() {
        final Long alertId = 9l;
        final AlertType alertType = AlertType.CriticalCold;
        final boolean closed = true;
        final String date = "2015-11-23T17:46";
        final Long notificationId = 8l;
        final Long shipmentId = 7l;
        final String title = "JUnit Alert";
        final NotificationType type = NotificationType.Alert;
        final String line1 = "line 1";
        final String line2 = "line 2";

        NotificationItem n= new NotificationItem();
        n.setAlertId(alertId);
        n.setAlertType(alertType);
        n.setClosed(closed);
        n.setDate(date);
        n.setNotificationId(notificationId);
        n.setShipmentId(shipmentId);
        n.setTitle(title);
        n.setType(type);
        n.getLines().add(line1);
        n.getLines().add(line2 );

        final JsonObject json = serializer.toJson(n);
        n = serializer.parseNotification(json);

        //check notification
        assertEquals(alertId, n.getAlertId());
        assertEquals(alertType, n.getAlertType());
        assertEquals(closed, n.isClosed());
        assertEquals(date, n.getDate());
        assertEquals(notificationId, n.getNotificationId());
        assertEquals(shipmentId, n.getShipmentId());
        assertEquals(title, n.getTitle());
        assertEquals(type, n.getType());
        assertEquals(line1, n.getLines().get(0));
        assertEquals(line2 , n.getLines().get(1));
    }

    @Test
    public void testArrivalNotification() {
        final Long alertId = 9l;
        final boolean closed = true;
        final String date = "2015-11-23T17:46";
        final Long notificationId = 8l;
        final Long shipmentId = 7l;
        final String title = "JUnit Alert";
        final NotificationType type = NotificationType.Arrival;

        NotificationItem n= new NotificationItem();
        n.setAlertId(alertId);
        n.setClosed(closed);
        n.setDate(date);
        n.setNotificationId(notificationId);
        n.setShipmentId(shipmentId);
        n.setTitle(title);
        n.setType(type);

        final JsonObject json = serializer.toJson(n);
        n = serializer.parseNotification(json);

        //check notification
        assertEquals(alertId, n.getAlertId());
        assertEquals(closed, n.isClosed());
        assertEquals(date, n.getDate());
        assertEquals(notificationId, n.getNotificationId());
        assertEquals(shipmentId, n.getShipmentId());
        assertEquals(title, n.getTitle());
        assertEquals(type, n.getType());
    }
}
