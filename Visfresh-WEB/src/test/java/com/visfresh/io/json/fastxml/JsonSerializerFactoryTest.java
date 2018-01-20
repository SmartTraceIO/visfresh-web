/**
 *
 */
package com.visfresh.io.json.fastxml;

import static org.junit.Assert.assertNull;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Date;

import org.junit.Test;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonObject;
import com.visfresh.entities.AlertType;
import com.visfresh.entities.CorrectiveAction;
import com.visfresh.entities.Location;
import com.visfresh.entities.ShipmentStatus;
import com.visfresh.entities.TrackerEventType;
import com.visfresh.io.shipment.AlertBean;
import com.visfresh.io.shipment.AlertProfileBean;
import com.visfresh.io.shipment.AlertRuleBean;
import com.visfresh.io.shipment.ArrivalBean;
import com.visfresh.io.shipment.CorrectiveActionListBean;
import com.visfresh.io.shipment.DeviceGroupDto;
import com.visfresh.io.shipment.InterimStopBean;
import com.visfresh.io.shipment.LocationProfileBean;
import com.visfresh.io.shipment.NoteBean;
import com.visfresh.io.shipment.ShipmentCompanyDto;
import com.visfresh.io.shipment.ShipmentUserDto;
import com.visfresh.io.shipment.SingleShipmentBean;
import com.visfresh.io.shipment.SingleShipmentData;
import com.visfresh.io.shipment.SingleShipmentLocationBean;
import com.visfresh.io.shipment.TemperatureAlertBean;
import com.visfresh.io.shipment.TemperatureRuleBean;
import com.visfresh.lists.ListNotificationScheduleItem;
import com.visfresh.utils.SerializerUtils;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class JsonSerializerFactoryTest {
    /**
     * Default constructor.
     */
    public JsonSerializerFactoryTest() {
        super();
    }

    @Test
    public void testCreateSingleShipmentDataParser() throws JsonGenerationException, JsonMappingException, JsonParseException, IOException {
        final SingleShipmentData data = new SingleShipmentData();

        final SingleShipmentBean s = new SingleShipmentBean();
        data.setBean(s);
        s.setStatus(ShipmentStatus.Arrived);
        s.setAlertsSuppressed(true);
        s.setAlertsSuppressionTime(new Date(System.currentTimeMillis() - 100000111l));
        s.setAlertSuppressionMinutes(34);
        s.setArrivalNotificationWithinKm(9);
        s.setArrivalReportSent(true);
        s.setArrivalTime(new Date(System.currentTimeMillis() - 29487392587l));
        s.setAssetNum("3298793");
        s.setAssetType("qpt8hkjn");
        s.setBatteryLevel(3456);
        s.setCommentsForReceiver("Any comments for receiver");
        s.setCompanyId(33l);
        s.setCurrentLocation(new Location(12.13, 14.15));
        s.setCurrentLocationDescription("Undetermined location");
        s.setDeviceColor("red");
        s.setDeviceName("JUnit Device");
        s.setDevice("2309873209847987");
        s.setEta(new Date(System.currentTimeMillis() - 9187023947908l));
        s.setExcludeNotificationsIfNoAlerts(true);
        s.setFirstReadingTime(new Date(System.currentTimeMillis() - 19809870987l));
        s.setLastReadingTemperature(93.);
        s.setLatestShipment(true);
        s.setMaxTemp(45.5);
        s.setMinTemp(6.6);
        s.setNoAlertsAfterArrivalMinutes(40);
        s.setNoAlertsAfterStartMinutes(57);
        s.setPalletId("palen N3");
        s.setPercentageComplete(87);
        s.setSendArrivalReport(true);
        s.setSendArrivalReportOnlyIfAlerts(true);
        s.setShipmentDescription("JUnit shipment");
        s.setShipmentId(33l);
        s.setShipmentType("JUnit");
        s.setShutDownAfterStartMinutes(95);
        s.setShutdownDeviceAfterMinutes(67);
        s.setShutdownTime(new Date(System.currentTimeMillis() - 192837987987l));
        s.setStartTime(new Date(System.currentTimeMillis() - 98709870987l));
        s.setStatus(ShipmentStatus.Ended);
        s.setTripCount(19);

        //alert profile
        final CorrectiveActionListBean a = new CorrectiveActionListBean();
        a.setDescription("Actions description");
        a.setId(7l);
        a.setName("Action list name");
        a.getActions().add(new CorrectiveAction("A", true));
        a.getActions().add(new CorrectiveAction("B", false));

        final AlertProfileBean ap = new AlertProfileBean();
        ap.setBatteryLowCorrectiveActions(a);
        s.setAlertProfile(ap);

        //alert notification schedule
        final ListNotificationScheduleItem alns = new ListNotificationScheduleItem();
        alns.setNotificationScheduleDescription("Schedule description");
        alns.setNotificationScheduleId(57l);
        alns.setNotificationScheduleName("Schedule Name");
        alns.setPeopleToNotify("Too many peoples");

        s.getAlertsNotificationSchedules().add(alns);

        //arrival
        final ArrivalBean arrival = new ArrivalBean();

        arrival.setDate(new Date(System.currentTimeMillis() - 19823797l));
        arrival.setId(14l);
        arrival.setMettersForArrival(90000);
        arrival.setNotifiedAt(new Date(System.currentTimeMillis() - 2395798l));
        arrival.setTrackerEventId(8l);
        s.setArrival(arrival);

        //arrival notification schedule.
        final ListNotificationScheduleItem arns = new ListNotificationScheduleItem();
        arns.setNotificationScheduleDescription("Schedule description");
        arns.setNotificationScheduleId(57);
        arns.setNotificationScheduleName("Schedule Name");
        arns.setPeopleToNotify("Too many peoples");

        s.getArrivalNotificationSchedules().add(arns);
        //add one empty only for check of count
        s.getArrivalNotificationSchedules().add(new ListNotificationScheduleItem());

        //start location
        final LocationProfileBean startLoc = new LocationProfileBean();
        startLoc.setAddress("Odessa, Derebasovskaya st.");
        startLoc.setCompanyName("SmartTrace LLC");
        startLoc.setId(7l);
        startLoc.setInterim(true);
        startLoc.setName("Location name");
        startLoc.setNotes("Location notest");
        startLoc.setRadius(857);
        startLoc.setStart(true);
        startLoc.setStop(true);

        s.setStartLocation(startLoc);

        //end location
        final LocationProfileBean endLoc = new LocationProfileBean();
        endLoc.setAddress("Odessa, Derebasovskaya st.");
        endLoc.setCompanyName("SmartTrace LLC");
        endLoc.setId(7l);
        endLoc.setInterim(true);
        endLoc.setName("Location name");
        endLoc.setNotes("location notes");
        endLoc.setRadius(957);
        endLoc.setStart(true);
        endLoc.setStop(true);

        s.setEndLocation(endLoc);

        //readings
        final SingleShipmentLocationBean loc = new SingleShipmentLocationBean();
        data.getLocations().add(loc);
        loc.setId(58l);
        loc.setLatitude(7.7);
        loc.setLongitude(8.8);
        loc.setTemperature(34.54);
        loc.setTime(new Date(System.currentTimeMillis() - 1089847l));
        loc.setType(TrackerEventType.AUT);

        //battery alert
        final AlertBean batteryAlert = new AlertBean();
        batteryAlert.setDate(new Date(System.currentTimeMillis() - 9234709l));
        batteryAlert.setId(99l);
        batteryAlert.setTrackerEventId(89l);
        batteryAlert.setType(AlertType.Battery);

        loc.getAlerts().add(batteryAlert);
        loc.getAlerts().add(batteryAlert);

        //temperature alert
        final TemperatureAlertBean ta = new TemperatureAlertBean();
        ta.setDate(new Date(System.currentTimeMillis() - 9234709l));
        ta.setId(99l);
        ta.setTrackerEventId(89l);
        ta.setType(AlertType.Hot);
        ta.setCumulative(true);
        ta.setMinutes(456);
        ta.setRuleId(87l);
        ta.setTemperature(36.6);

        loc.setType(TrackerEventType.AUT);
        loc.getAlerts().add(ta);

        //alert yet to fire simple
        final AlertRuleBean simpleAef = new AlertRuleBean();
        simpleAef.setId(11l);
        simpleAef.setType(AlertType.Battery);

        s.getAlertYetToFire().add(simpleAef);
        s.getAlertYetToFire().add(simpleAef);

        //temperature alert yet to fire
        final TemperatureRuleBean tempAef = new TemperatureRuleBean();
        tempAef.setId(11l);
        tempAef.setType(AlertType.CriticalCold);
        tempAef.setCumulativeFlag(true);
        tempAef.setMaxRateMinutes(17);
        tempAef.setTimeOutMinutes(54);
        tempAef.setTemperature(55.5);

        final CorrectiveActionListBean aefCa = new CorrectiveActionListBean();
        aefCa.setDescription("Actions description");
        aefCa.setId(7l);
        aefCa.setName("Action list name");
        aefCa.getActions().add(new CorrectiveAction("A", true));
        aefCa.getActions().add(new CorrectiveAction("B", false));
        tempAef.setCorrectiveActions(aefCa);

        s.getAlertYetToFire().add(tempAef);

        //simple fired alert
        final AlertRuleBean simpleAf = new AlertRuleBean();
        simpleAf.setId(11l);
        simpleAf.setType(AlertType.Battery);

        s.getAlertFired().add(simpleAf);
        s.getAlertFired().add(simpleAf);

        //fired temperature alert
        final TemperatureRuleBean taf = new TemperatureRuleBean();
        taf.setId(11l);
        taf.setType(AlertType.CriticalHot);
        taf.setCumulativeFlag(true);
        taf.setMaxRateMinutes(17);
        taf.setTimeOutMinutes(54);
        taf.setTemperature(55.5);

        s.getAlertFired().add(taf);

        //alert fired temperature action
        final CorrectiveActionListBean afca = new CorrectiveActionListBean();
        afca.setDescription("Actions description");
        afca.setId(7l);
        afca.setName("Action list name");
        afca.getActions().add(new CorrectiveAction("A", true));
        afca.getActions().add(new CorrectiveAction("B", false));
        taf.setCorrectiveActions(afca);

        //start location alternatives
        final LocationProfileBean altStart = new LocationProfileBean();
        altStart.setAddress("Odessa, Derivasovskaya st.");
        altStart.setCompanyName("Company name");
        altStart.setId(34l);
        altStart.setInterim(true);
        altStart.setName("Alternative 1");
        altStart.setNotes("alt loc notes");
        altStart.setRadius(450);
        altStart.setStart(true);
        altStart.setStop(true);

        s.getStartLocationAlternatives().add(altStart);
        s.getStartLocationAlternatives().add(altStart);

        //end location alternatives
        final LocationProfileBean altEnd = new LocationProfileBean();
        altEnd.setAddress("Odessa, Derivasovskaya st.");
        altEnd.setCompanyName("SmartTrace");
        altEnd.setId(98l);
        altEnd.setInterim(true);
        altEnd.setName("End location alternative");
        altEnd.setNotes("end loc notes");
        altEnd.setRadius(999);
        altEnd.setStart(true);
        altEnd.setStop(true);

        s.getEndLocationAlternatives().add(altEnd);
        s.getEndLocationAlternatives().add(altEnd);

        //end location alternatives
        final LocationProfileBean altInt = new LocationProfileBean();
        altInt.setAddress("Odessa, Derivasovskaya st.");
        altInt.setCompanyName("SmartTrace LLC");
        altInt.setId(45l);
        altInt.setInterim(true);
        altInt.setName("Interim Location aleternative");
        altInt.setNotes("int loc notes");
        altInt.setRadius(3443);
        altInt.setStart(true);
        altInt.setStop(true);

        s.getInterimLocationAlternatives().add(altInt);
        s.getInterimLocationAlternatives().add(altInt);

        //interim stops
        final LocationProfileBean intStopLoc = new LocationProfileBean();
        intStopLoc.setAddress("Odessa, Derivasovskaya st.");
        intStopLoc.setCompanyName("Company name");
        intStopLoc.setId(2l);
        intStopLoc.setInterim(true);
        intStopLoc.setName("Location name");
        intStopLoc.setNotes("interim stop location");
        intStopLoc.setRadius(555);
        intStopLoc.setStart(true);
        intStopLoc.setStop(true);

        final InterimStopBean stp = new InterimStopBean();
        stp.setId(45l);
        stp.setLocation(intStopLoc);
        stp.setStopDate(new Date(System.currentTimeMillis() - 1000098098l));
        stp.setTime(15);

        s.getInterimStops().add(stp);
        s.getInterimStops().add(stp);


        //user access
        final ShipmentUserDto user = new ShipmentUserDto();
        user.setEmail("junit@smarttrace.com.au");
        user.setId(99l);

        s.getUserAccess().add(user);
        s.getUserAccess().add(user);

        //company access
        final ShipmentCompanyDto companyAccess = new ShipmentCompanyDto();
        companyAccess.setId(12l);
        companyAccess.setName("Company Name");

        s.getCompanyAccess().add(companyAccess);
        s.getCompanyAccess().add(companyAccess);

        //simple sent alert
        final AlertBean simpleSentAlert = new AlertBean();
        simpleSentAlert.setDate(new Date(System.currentTimeMillis() - 9234709l));
        simpleSentAlert.setId(99l);
        simpleSentAlert.setTrackerEventId(89l);
        simpleSentAlert.setType(AlertType.Battery);

        s.getSentAlerts().add(simpleSentAlert);
        s.getSentAlerts().add(simpleSentAlert);

        //sent temperature alert
        final TemperatureAlertBean tempAlertSent = new TemperatureAlertBean();
        tempAlertSent.setDate(new Date(System.currentTimeMillis() - 9234709l));
        tempAlertSent.setId(99l);
        tempAlertSent.setTrackerEventId(89l);
        tempAlertSent.setType(AlertType.Hot);
        tempAlertSent.setCumulative(true);
        tempAlertSent.setMinutes(456);
        tempAlertSent.setRuleId(67l);
        tempAlertSent.setTemperature(36.6);

        //alert profile light on corrective action
        final CorrectiveActionListBean apca = new CorrectiveActionListBean();
        apca.setDescription("Light On");
        apca.setId(5l);
        apca.setName("Light On");
        apca.getActions().add(new CorrectiveAction("A", true));
        apca.getActions().add(new CorrectiveAction("B", false));

        ap.setLightOnCorrectiveActions(apca);

        //notes
        final NoteBean note = new NoteBean();
        note.setActive(true);
        note.setCreatedByName("Created By Name");
        note.setCreatedBy("Created By");
        note.setCreationDate(new Date(System.currentTimeMillis() - 1923470987l));
        note.setNoteNum(1);
        note.setNoteText("Note Text");
        note.setNoteType("Note Type");
        note.setTimeOnChart(new Date(System.currentTimeMillis() - 129087009l));

        s.getNotes().add(note);
        s.getNotes().add(note);

        //device groups
        final DeviceGroupDto group = new DeviceGroupDto();
        group.setDescription("Device group description");
        group.setId(19l);
        group.setName("Device Group Name");

        s.getDeviceGroups().add(group);
        s.getDeviceGroups().add(group);


        //run test
        final StringWriter jsonOrigin = new StringWriter();
        final ObjectMapper ser = JsonSerializerFactory.FACTORY.createDefaultMapper();
        ser.writeValue(jsonOrigin, data);

        final StringWriter jsonNew = new StringWriter();
        ser.writeValue(jsonNew, JsonSerializerFactory.FACTORY.createSingleShipmentDataParser().readValue(
                new StringReader(jsonOrigin.toString()), SingleShipmentData.class));

        //compare results
        final JsonObject merge = SerializerUtils.diff(
                SerializerUtils.parseJson(jsonOrigin.toString()).getAsJsonObject(),
                SerializerUtils.parseJson(jsonNew.toString()).getAsJsonObject());

        assertNull(merge);
    }
}
