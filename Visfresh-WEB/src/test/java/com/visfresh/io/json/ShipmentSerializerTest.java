/**
 *
 */
package com.visfresh.io.json;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

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
import com.visfresh.io.GetFilteredShipmentsRequest;
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
        serializer.setUserResolver(resolver);
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
        final ShipmentStatus status = ShipmentStatus.Ended;
        final String assetType = "Trailer";
        final String assetNum = "10515";
        final int poNum = 938498;
        final int tripCount = 11;
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
        s.setShutdownDeviceAfterMinutes(shutdownDeviceTimeOut);
        s.setDevice(device);
        s.setPalletId(palletId);
        s.setShipmentDate(shipmentDate);
        s.getCustomFields().put("name", "value");
        s.setStatus(status);
        s.setAssetType(assetType);
        s.setAssetNum(assetNum);
        s.setPoNum(poNum);
        s.setTripCount(tripCount);
        s.setCommentsForReceiver(commentsForReceiver);

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
        assertEquals(device.getId(), s.getDevice().getId());
        assertEquals(palletId, s.getPalletId());
        assertEquals(format(shipmentDate), format(s.getShipmentDate()));
        assertEquals("value", s.getCustomFields().get("name"));
        assertEquals(status, s.getStatus());
        assertEquals(assetType, s.getAssetType());
        assertEquals(assetNum, s.getAssetNum());
        assertEquals(poNum, s.getPoNum());
        assertEquals(tripCount, s.getTripCount());
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
    @Test
    public void testGetFilteredShipmentsRequest() {
        //test all nulls
        GetFilteredShipmentsRequest req = new GetFilteredShipmentsRequest();

        JsonObject json = serializer.toJson(req);
        req = serializer.parseGetFilteredShipmentsRequest(json);

        assertFalse(req.isAlertsOnly());
        assertNull(req.getDeviceImei());
        assertNull(req.getLast2Days());
        assertNull(req.getLastDay());
        assertNull(req.getLastMonth());
        assertNull(req.getLastWeek());
        assertNull(req.getShipmentDateFrom());
        assertNull(req.getShipmentDateTo());
        assertNull(req.getShipmentDescription());
        assertNull(req.getShippedFrom());
        assertNull(req.getShippedTo());
        assertNull(req.getStatus());

        //test not null values
        req = new GetFilteredShipmentsRequest();

        final String deviceImei = "283409237873234";
        final Boolean last2Days = true;
        final Boolean lastDay = true;
        final Boolean lastMonth = true;
        final Boolean lastWeek = true;
        final Date shipmentDateFrom = new Date(System.currentTimeMillis() - 1000000000l);
        final Date shipmentDateTo = new Date(System.currentTimeMillis() - 1000000l);
        final String shipmentDescription = "JUnit Shipment";
        final List<Long> shippedFrom = new LinkedList<Long>();
        shippedFrom.add(4l);
        shippedFrom.add(5l);
        final List<Long> shippedTo = new LinkedList<Long>();
        shippedTo.add(1l);
        shippedTo.add(2l);
        shippedTo.add(3l);
        final ShipmentStatus status = ShipmentStatus.InProgress;
        final Integer pageIndex = 10;
        final Integer pageSize = 200;
        final String sortColumn = "anyColumn";
        final String sortOrder = "asc";

        req.setAlertsOnly(true);
        req.setDeviceImei(deviceImei);
        req.setLast2Days(last2Days);
        req.setLastDay(lastDay);
        req.setLastMonth(lastMonth);
        req.setLastWeek(lastWeek);
        req.setShipmentDateFrom(shipmentDateFrom);
        req.setShipmentDateTo(shipmentDateTo);
        req.setShipmentDescription(shipmentDescription);
        req.setShippedFrom(shippedFrom);
        req.setShippedTo(shippedTo);
        req.setStatus(status);
        req.setPageIndex(pageIndex);
        req.setPageSize(pageSize);
        req.setSortColumn(sortColumn);
        req.setSortOrder(sortOrder);

        json = serializer.toJson(req);
        req = serializer.parseGetFilteredShipmentsRequest(json);

        assertTrue(req.isAlertsOnly());
        assertEquals(deviceImei, req.getDeviceImei());
        assertEquals(last2Days, req.getLast2Days());
        assertEquals(lastDay, req.getLastDay());
        assertEquals(lastMonth, req.getLastMonth());
        assertEquals(lastWeek, req.getLastWeek());
        assertEquals(formatDate(shipmentDateFrom), formatDate(req.getShipmentDateFrom()));
        assertEquals(formatDate(shipmentDateTo), formatDate(req.getShipmentDateTo()));
        assertEquals(shipmentDescription, req.getShipmentDescription());
        assertEquals(2, req.getShippedFrom().size());
        assertEquals(3, req.getShippedTo().size());
        assertEquals(status, req.getStatus());
        assertEquals(pageIndex, req.getPageIndex());
        assertEquals(pageSize, req.getPageSize());
        assertEquals(sortColumn, req.getSortColumn());
        assertEquals(sortOrder, req.getSortOrder());
    }

    /**
     * @param date
     * @return
     */
    private String formatDate(final Date date) {
        return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm").format(date);
    }
}
