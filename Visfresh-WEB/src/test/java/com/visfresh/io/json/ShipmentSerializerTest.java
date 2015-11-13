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
import com.visfresh.entities.AlertProfile;
import com.visfresh.entities.Device;
import com.visfresh.entities.LocationProfile;
import com.visfresh.entities.NotificationSchedule;
import com.visfresh.entities.Shipment;
import com.visfresh.entities.ShipmentStatus;
import com.visfresh.entities.User;
import com.visfresh.io.SaveShipmentRequest;
import com.visfresh.io.SaveShipmentResponse;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class ShipmentSerializerTest extends AbstractSerializerTest {
    private ShipmentSerializer serializer;

    /**
     * Default constructor.
     */
    public ShipmentSerializerTest() {
        super();
    }

    /**
     * Initializes the test.
     */
    @Before
    public void setUp() {
        final User user = new User();
        user.setTimeZone(UTC);

        serializer = new ShipmentSerializer(user);
        serializer.setReferenceResolver(resolver);
    }

    @Test
    public void testShipment() {
        final AlertProfile alertProfile = createAlertProfile();
        final NotificationSchedule alertsNotificationSchedule = createNotificationSchedule();
        final int alertSuppressionDuringCoolDown = 55;
        final int arrivalNotification = 111;
        final NotificationSchedule arrivalNotificationSchedule = createNotificationSchedule();
        final boolean excludeNotificationsIfNoAlertsFired = true;
        final Long id = 77l;
        final String shipmentDescription = "Any Description";
        final LocationProfile shippedFrom = createLocationProfile();
        final LocationProfile shippedTo = createLocationProfile();
        final int shutdownDeviceTimeOut = 155;
        final Device device = createDevice("234908720394857");
        final String palletId = "palettid";
        final Date shipmentDate = new Date(System.currentTimeMillis() - 1000000000l);
        final ShipmentStatus status = ShipmentStatus.Complete;
        final String assetType = "Trailer";
        final String assetNum = "10515";
        final int poNum = 938498;
        final int tripCount = 11;
        final int maxTimesAlertFires = 14;
        final String commentsForReceiver = "commentsForReceiver";

        Shipment s = new Shipment();
        s.setAlertProfile(alertProfile);
        s.getAlertsNotificationSchedules().add(alertsNotificationSchedule);
        s.setAlertSuppressionMinutes(alertSuppressionDuringCoolDown);
        s.setArrivalNotificationWithinKm(arrivalNotification);
        s.getArrivalNotificationSchedules().add(arrivalNotificationSchedule);
        s.setExcludeNotificationsIfNoAlerts(excludeNotificationsIfNoAlertsFired);
        s.setId(id);
        s.setShipmentDescription(shipmentDescription);
        s.setShippedFrom(shippedFrom);
        s.setShippedTo(shippedTo);
        s.setShutdownDeviceTimeOut(shutdownDeviceTimeOut);
        s.setDevice(device);
        s.setPalletId(palletId);
        s.setShipmentDate(shipmentDate);
        s.getCustomFields().put("name", "value");
        s.setStatus(status);
        s.setAssetType(assetType);
        s.setAssetNum(assetNum);
        s.setPoNum(poNum);
        s.setTripCount(tripCount);
        s.setMaxTimesAlertFires(maxTimesAlertFires);
        s.setCommentsForReceiver(commentsForReceiver);

        final JsonObject obj = serializer.toJson(s).getAsJsonObject();
        s = serializer.parseShipment(obj);

        assertNotNull(s.getAlertProfile());
        assertNotNull(s.getAlertsNotificationSchedules());
        assertEquals(alertSuppressionDuringCoolDown, s.getAlertSuppressionMinutes());
        assertEquals(arrivalNotification, s.getArrivalNotificationWithinKm());
        assertNotNull(s.getArrivalNotificationSchedules());
        assertEquals(excludeNotificationsIfNoAlertsFired, s.isExcludeNotificationsIfNoAlerts());
        assertEquals(id, s.getId());
        assertEquals(shipmentDescription, s.getShipmentDescription());
        assertNotNull(s.getShippedFrom());
        assertNotNull(s.getShippedTo());
        assertEquals(shutdownDeviceTimeOut, s.getShutdownDeviceTimeOut());
        assertEquals(device.getId(), s.getDevice().getId());
        assertEquals(palletId, s.getPalletId());
        assertEquals(format(shipmentDate), format(s.getShipmentDate()));
        assertEquals("value", s.getCustomFields().get("name"));
        assertEquals(status, s.getStatus());
        assertEquals(assetType, s.getAssetType());
        assertEquals(assetNum, s.getAssetNum());
        assertEquals(poNum, s.getPoNum());
        assertEquals(tripCount, s.getTripCount());
        assertEquals(maxTimesAlertFires, s.getMaxTimesAlertFires());
        assertEquals(commentsForReceiver, s.getCommentsForReceiver());
    }
    @Test
    public void testSaveShipmentResponse() {
        final Long shipmentId = 1L;
        final Long templateId = 2l;

        SaveShipmentResponse r = new SaveShipmentResponse();
        r.setShipmentId(shipmentId);
        r.setTemplateId(templateId);

        final JsonObject obj = serializer.toJson(r);
        r = serializer.parseSaveShipmentResponse(obj);

        assertEquals(shipmentId, r.getShipmentId());
        assertEquals(templateId, r.getTemplateId());
    }
    @Test
    public void testSaveShipmentRequest() {
        final boolean saveAsNewTemplate = true;
        final Shipment shipment = createShipment();
        final String templateName = "JUnit Shipment Template";

        SaveShipmentRequest req = new SaveShipmentRequest();
        req.setSaveAsNewTemplate(saveAsNewTemplate);
        req.setShipment(shipment);
        req.setTemplateName(templateName);

        final JsonObject obj = serializer.toJson(req);
        req = serializer.parseSaveShipmentRequest(obj);

        assertEquals(saveAsNewTemplate, req.isSaveAsNewTemplate());
        assertEquals(templateName, req.getTemplateName());
        assertNotNull(req.getShipment());
    }
}
