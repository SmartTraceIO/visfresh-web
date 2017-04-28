/**
 *
 */
package com.visfresh.io.json;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.junit.Before;
import org.junit.Test;

import com.google.gson.JsonObject;
import com.visfresh.entities.Device;
import com.visfresh.entities.ShipmentStatus;
import com.visfresh.entities.User;
import com.visfresh.io.SaveShipmentRequest;
import com.visfresh.io.SaveShipmentResponse;
import com.visfresh.io.ShipmentDto;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class ShipmentSerializerTest extends AbstractSerializerTest {
    private ShipmentSerializer serializer;
    private DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm");

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
    }

    @Test
    public void testShipment() {
        final Long alertProfile = 2l;
        final Long alertsNotificationSchedule = 3l;
        final int alertSuppressionDuringCoolDown = 55;
        final int arrivalNotification = 111;
        final Long arrivalNotificationSchedule = 4l;
        final boolean excludeNotificationsIfNoAlertsFired = true;
        final Long id = 77l;
        final String shipmentDescription = "Any Description";
        final Long shippedFrom = 5l;
        final Long shippedTo = 6l;
        final int shutdownDeviceTimeOut = 155;
        final Device device = createDevice("234908720394857");
        final String palletId = "palettid";
        final Date shipmentDate = new Date(System.currentTimeMillis() - 1000000000l);
        final ShipmentStatus status = ShipmentStatus.Ended;
        final String assetType = "Trailer";
        final String assetNum = "10515";
        final int poNum = 938498;
        final int tripCount = 11;
        final String commentsForReceiver = "commentsForReceiver";
        final Integer noAlertsAfterArrivalMinutes = 3;
        final Integer noAlertsAfterStartMinutes = 33;
        final Integer shutDownAfterStartMinutes = 5;
        final String createdBy = "developer";
        final Date startDate = new Date(10000l);
        final Date deviceShutdownTime = new Date(923847092834l);
        final boolean sendArrivalReport = false;
        final boolean sendArrivalReportOnlyIfAlerts = true;
        final Long externalUser = 14l;
        final Long externalCompany = 15l;

        ShipmentDto s = new ShipmentDto();
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
        s.setShutdownDeviceAfterMinutes(shutdownDeviceTimeOut);
        s.setDeviceImei(device.getImei());
        s.setDeviceName(device.getName());
        s.setDeviceSN(device.getSn());
        s.setPalletId(palletId);
        s.setShipmentDate(shipmentDate);
        s.getCustomFields().put("name", "value");
        s.setStatus(status);
        s.setAssetType(assetType);
        s.setAssetNum(assetNum);
        s.setPoNum(poNum);
        s.setTripCount(tripCount);
        s.setCommentsForReceiver(commentsForReceiver);
        s.setNoAlertsAfterArrivalMinutes(noAlertsAfterArrivalMinutes);
        s.setNoAlertsAfterStartMinutes(noAlertsAfterStartMinutes);
        s.setShutDownAfterStartMinutes(shutDownAfterStartMinutes);
        s.setCreatedBy(createdBy);
        s.setStartDate(startDate);
        s.setDeviceShutdownTime(deviceShutdownTime);
        s.setSendArrivalReport(sendArrivalReport);
        s.setSendArrivalReportOnlyIfAlerts(sendArrivalReportOnlyIfAlerts);
        s.getUserAccess().add(externalUser);
        s.getCompanyAccess().add(externalCompany);

        final JsonObject obj = serializer.toJson(s).getAsJsonObject();
        s = serializer.parseShipment(obj);

        assertNotNull(s.getAlertProfile());
        assertNotNull(s.getAlertsNotificationSchedules());
        assertEquals(alertSuppressionDuringCoolDown, s.getAlertSuppressionMinutes());
        assertEquals(arrivalNotification, s.getArrivalNotificationWithinKm().intValue());
        assertNotNull(s.getArrivalNotificationSchedules());
        assertEquals(excludeNotificationsIfNoAlertsFired, s.isExcludeNotificationsIfNoAlerts());
        assertEquals(id, s.getId());
        assertEquals(shipmentDescription, s.getShipmentDescription());
        assertNotNull(s.getShippedFrom());
        assertNotNull(s.getShippedTo());
        assertEquals(shutdownDeviceTimeOut, s.getShutdownDeviceAfterMinutes().intValue());
        assertEquals(device.getImei(), s.getDeviceImei());
        assertEquals(device.getSn(), s.getDeviceSN());
        assertEquals(device.getName(), s.getDeviceName());
        assertEquals(palletId, s.getPalletId());
        assertEquals(format(shipmentDate), format(s.getShipmentDate()));
        assertEquals("value", s.getCustomFields().get("name"));
        assertEquals(status, s.getStatus());
        assertEquals(assetType, s.getAssetType());
        assertEquals(assetNum, s.getAssetNum());
        assertEquals(poNum, s.getPoNum());
        assertEquals(tripCount, s.getTripCount());
        assertEquals(commentsForReceiver, s.getCommentsForReceiver());
        assertEquals(noAlertsAfterArrivalMinutes, s.getNoAlertsAfterArrivalMinutes());
        assertEquals(noAlertsAfterStartMinutes, s.getNoAlertsAfterStartMinutes());
        assertEquals(shutDownAfterStartMinutes, s.getShutDownAfterStartMinutes());
        assertEquals(createdBy, s.getCreatedBy());
        assertEquals(dateFormat.format(startDate), dateFormat.format(s.getStartDate()));
        assertEquals(dateFormat.format(deviceShutdownTime), dateFormat.format(s.getDeviceShutdownTime()));
        assertEquals(sendArrivalReport, s.isSendArrivalReport());
        assertEquals(sendArrivalReportOnlyIfAlerts, s.isSendArrivalReportOnlyIfAlerts());
        assertEquals(externalUser, s.getUserAccess().get(0));
        assertEquals(externalCompany, s.getCompanyAccess().get(0));
    }
    @Test
    public void testShipmentDefaults() {
        ShipmentDto s = new ShipmentDto();
        s.setId(77l);

        final JsonObject obj = serializer.toJson(s).getAsJsonObject();
        s = serializer.parseShipment(obj);

        assertNull(s.getAlertProfile());
        assertNotNull(s.getAlertsNotificationSchedules());
        assertNotNull(s.getArrivalNotificationSchedules());
        assertNull(s.getShippedFrom());
        assertNull(s.getShippedTo());
        assertTrue(s.isSendArrivalReport());
        assertFalse(s.isSendArrivalReportOnlyIfAlerts());
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
        final ShipmentDto shipment = new ShipmentDto(createShipment());
        final String templateName = "JUnit Shipment Template";
        final Boolean includePreviousData = true;

        SaveShipmentRequest req = new SaveShipmentRequest();
        req.setSaveAsNewTemplate(saveAsNewTemplate);
        req.setShipment(shipment);
        req.setTemplateName(templateName);
        req.setIncludePreviousData(includePreviousData);

        final JsonObject obj = serializer.toJson(req);
        req = serializer.parseSaveShipmentRequest(obj);

        assertEquals(saveAsNewTemplate, req.isSaveAsNewTemplate());
        assertEquals(templateName, req.getTemplateName());
        assertEquals(includePreviousData, req.isIncludePreviousData());
        assertNotNull(req.getShipment());
    }
}
