/**
 *
 */
package com.visfresh.io.json;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Before;
import org.junit.Test;

import com.google.gson.JsonObject;
import com.visfresh.entities.AlertProfile;
import com.visfresh.entities.LocationProfile;
import com.visfresh.entities.NotificationSchedule;
import com.visfresh.entities.ShipmentTemplate;
import com.visfresh.services.lists.ListShipmentTemplateItem;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class ShipmentTemplateSerializerTest extends AbstractSerializerTest {
    private ShipmentTemplateSerializer serializer = new ShipmentTemplateSerializer(UTC);

    /**
     * Default constructor.
     */
    public ShipmentTemplateSerializerTest() {
        super();
    }

    @Before
    public void setUp() {
        serializer.setReferenceResolver(resolver);
    }
    @Test
    public void testListShipmentTemplateItem() {
        ListShipmentTemplateItem item = new ListShipmentTemplateItem();

        final Long alertProfile = 1l;
        final String alertProfileName = "Alert Profile Name";
        final String shipmentDescription = "Shipment Description";
        final long shipmentTemplateId = 2l;
        final String shipmentTemplateName = "Shipment Template Name";
        final Long shippedFrom = 3L;
        final String shippedFromLocationName = "Shipped from location name";
        final Long shippedTo = 4l;
        final String shippedToLocationName = "Shipped to location name";

        item.setAlertProfile(alertProfile);
        item.setAlertProfileName(alertProfileName);
        item.setShipmentDescription(shipmentDescription);
        item.setShipmentTemplateId(shipmentTemplateId);
        item.setShipmentTemplateName(shipmentTemplateName);
        item.setShippedFrom(shippedFrom);
        item.setShippedFromLocationName(shippedFromLocationName);
        item.setShippedTo(shippedTo);
        item.setShippedToLocationName(shippedToLocationName);

        final JsonObject obj = serializer.toJson(item);
        item = serializer.parseListShipmentTemplateItem(obj);

        assertEquals(alertProfile, item.getAlertProfile());
        assertEquals(alertProfileName, item.getAlertProfileName());
        assertEquals(shipmentDescription, item.getShipmentDescription());
        assertEquals(shipmentTemplateId, item.getShipmentTemplateId());
        assertEquals(shipmentTemplateName, item.getShipmentTemplateName());
        assertEquals(shippedFrom, item.getShippedFrom());
        assertEquals(shippedFromLocationName, item.getShippedFromLocationName());
        assertEquals(shippedTo, item.getShippedTo());
        assertEquals(shippedToLocationName, item.getShippedToLocationName());
    }

    @Test
    public void testShipmentTemplate() {
        final boolean addDateShipped = true;
        final AlertProfile alertProfile = createAlertProfile();
        final NotificationSchedule alertsNotificationSchedule = createNotificationSchedule();
        final int alertSuppressionDuringCoolDown = 55;
        final int arrivalNotification = 111;
        final NotificationSchedule arrivalNotificationSchedule = createNotificationSchedule();
        final boolean excludeNotificationsIfNoAlertsFired = true;
        final Long id = 77l;
        final String name = "JUnit-tpl";
        final String shipmentDescription = "Any Description";
        final LocationProfile shippedFrom = createLocationProfile();
        final LocationProfile shippedTo = createLocationProfile();
        final int shutdownDeviceTimeOut = 155;
        final boolean useLocationNearestToDevice = true;
        final String commentsForReceiver = "commentsForReceiver";

        ShipmentTemplate t = new ShipmentTemplate();
        t.setAddDateShipped(addDateShipped);
        t.setAlertProfile(alertProfile);
        t.getAlertsNotificationSchedules().add(alertsNotificationSchedule);
        t.setAlertSuppressionMinutes(alertSuppressionDuringCoolDown);
        t.setArrivalNotificationWithinKm(arrivalNotification);
        t.getArrivalNotificationSchedules().add(arrivalNotificationSchedule);
        t.setExcludeNotificationsIfNoAlerts(excludeNotificationsIfNoAlertsFired);
        t.setId(id);
        t.setName(name);
        t.setShipmentDescription(shipmentDescription);
        t.setShippedFrom(shippedFrom);
        t.setShippedTo(shippedTo);
        t.setShutdownDeviceTimeOut(shutdownDeviceTimeOut);
        t.setDetectLocationForShippedFrom(useLocationNearestToDevice);
        t.setCommentsForReceiver(commentsForReceiver);

        final JsonObject obj = serializer.toJson(t).getAsJsonObject();

        t = serializer.parseShipmentTemplate(obj);

        assertEquals(addDateShipped, t.isAddDateShipped());
        assertNotNull(t.getAlertProfile());
        assertNotNull(t.getAlertsNotificationSchedules());
        assertEquals(alertSuppressionDuringCoolDown, t.getAlertSuppressionMinutes());
        assertEquals(arrivalNotification, t.getArrivalNotificationWithinKm());
        assertNotNull(t.getArrivalNotificationSchedules());
        assertEquals(excludeNotificationsIfNoAlertsFired, t.isExcludeNotificationsIfNoAlerts());
        assertEquals(id, t.getId());
        assertEquals(name, t.getName());
        assertEquals(shipmentDescription, t.getShipmentDescription());
        assertNotNull(t.getShippedFrom());
        assertNotNull(t.getShippedTo());
        assertEquals(shutdownDeviceTimeOut, t.getShutdownDeviceTimeOut().intValue());
        assertEquals(useLocationNearestToDevice, t.isDetectLocationForShippedFrom());
        assertEquals(commentsForReceiver, t.getCommentsForReceiver());
    }
}
