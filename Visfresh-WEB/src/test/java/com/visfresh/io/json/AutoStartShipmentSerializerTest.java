/**
 *
 */
package com.visfresh.io.json;

import static org.junit.Assert.assertEquals;

import java.util.TimeZone;

import org.junit.Before;
import org.junit.Test;

import com.visfresh.io.AutoStartShipmentDto;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class AutoStartShipmentSerializerTest {
    private AutoStartShipmentSerializer serializer;

    /**
     * Default constructor.
     */
    public AutoStartShipmentSerializerTest() {
        super();
    }

    @Before
    public void setUp() {
        serializer = new AutoStartShipmentSerializer(TimeZone.getDefault());
    }

    @Test
    public void testSerialize() {
        AutoStartShipmentDto dto = new AutoStartShipmentDto();
        final int priority = 5;
        final Long id = 7l;
        final Long loc1 = 1l;
        final Long loc2 = 2l;
        final Long loc3 = 3l;
        final Long loc4 = 4l;

        final String locName1 = "Location_1";
        final String locName2 = "Location_2";
        final String locName3 = "Location_3";
        final String locName4 = "Location_4";

        //template
        final int alertSuppressionMinutes = 25;
        final Long alertProfile = 7l;
        final String alertProfileName = "JUnit alert profile";
        final Integer arrivalNotificationWithinKm = 15;
        final boolean excludeNotificationsIfNoAlerts = true;
        final Integer shutdownDeviceAfterMinutes = 99;
        final Integer noAlertsAfterArrivalMinutes = 43;
        final Integer noAlertsAfterStartMinutes = 433;
        final Integer shutDownAfterStartMinutes = 47;
        final String commentsForReceiver = "Any comments for receiver";
        final String name = "JUnit name";
        final String shipmentDescription = "JUnit shipment";
        final boolean addDateShipped = true;

        dto.setAlertSuppressionMinutes(alertSuppressionMinutes);
        dto.setAlertProfile(alertProfile);
        dto.setAlertProfileName(alertProfileName);
        dto.getAlertsNotificationSchedules().add(1L);
        dto.getAlertsNotificationSchedules().add(2L);
        dto.setArrivalNotificationWithinKm(arrivalNotificationWithinKm);
        dto.getArrivalNotificationSchedules().add(3l);
        dto.getArrivalNotificationSchedules().add(4l);
        dto.setExcludeNotificationsIfNoAlerts(excludeNotificationsIfNoAlerts);
        dto.setShutdownDeviceAfterMinutes(shutdownDeviceAfterMinutes);
        dto.setNoAlertsAfterArrivalMinutes(noAlertsAfterArrivalMinutes);
        dto.setNoAlertsAfterStartMinutes(noAlertsAfterStartMinutes);
        dto.setShutDownAfterStartMinutes(shutDownAfterStartMinutes);
        dto.setCommentsForReceiver(commentsForReceiver);
        dto.setName(name);
        dto.setShipmentDescription(shipmentDescription);
        dto.setAddDateShipped(addDateShipped);

        dto.setId(id);
        dto.setPriority(priority);
        dto.getStartLocations().add(loc1);
        dto.getStartLocationNames().add(locName1);
        dto.getStartLocations().add(loc2);
        dto.getStartLocationNames().add(locName2);
        dto.getEndLocations().add(loc3);
        dto.getEndLocationNames().add(locName3);
        dto.getEndLocations().add(loc4);
        dto.getEndLocationNames().add(locName4);

        dto = serializer.parseAutoStartShipmentDto(serializer.toJson(dto));

        assertEquals(id, dto.getId());
        assertEquals(priority, dto.getPriority());
        assertEquals(loc1, dto.getStartLocations().get(0));
        assertEquals(loc2, dto.getStartLocations().get(1));
        assertEquals(loc3, dto.getEndLocations().get(0));
        assertEquals(loc4, dto.getEndLocations().get(1));

        assertEquals(locName1, dto.getStartLocationNames().get(0));
        assertEquals(locName2, dto.getStartLocationNames().get(1));
        assertEquals(locName3, dto.getEndLocationNames().get(0));
        assertEquals(locName4, dto.getEndLocationNames().get(1));

        //template
        assertEquals(alertSuppressionMinutes, dto.getAlertSuppressionMinutes());
        assertEquals(alertProfile, dto.getAlertProfile());
        assertEquals(alertProfileName, dto.getAlertProfileName());
        assertEquals(1l, dto.getAlertsNotificationSchedules().get(0).longValue());
        assertEquals(2l, dto.getAlertsNotificationSchedules().get(1).longValue());
        assertEquals(arrivalNotificationWithinKm, dto.getArrivalNotificationWithinKm());
        assertEquals(3l, dto.getArrivalNotificationSchedules().get(0).longValue());
        assertEquals(4l, dto.getArrivalNotificationSchedules().get(1).longValue());
        assertEquals(excludeNotificationsIfNoAlerts, dto.isExcludeNotificationsIfNoAlerts());
        assertEquals(shutdownDeviceAfterMinutes, dto.getShutdownDeviceAfterMinutes());
        assertEquals(noAlertsAfterArrivalMinutes, dto.getNoAlertsAfterArrivalMinutes());
        assertEquals(noAlertsAfterStartMinutes, dto.getNoAlertsAfterStartMinutes());
        assertEquals(shutDownAfterStartMinutes, dto.getShutDownAfterStartMinutes());
        assertEquals(commentsForReceiver, dto.getCommentsForReceiver());
        assertEquals(name, dto.getName());
        assertEquals(shipmentDescription, dto.getShipmentDescription());
        assertEquals(addDateShipped, dto.isAddDateShipped());
    }
}
