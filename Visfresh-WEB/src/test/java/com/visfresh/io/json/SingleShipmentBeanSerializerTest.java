/**
 *
 */
package com.visfresh.io.json;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Date;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.gson.JsonObject;
import com.visfresh.dao.impl.json.ShortenerAliasesBuilder;
import com.visfresh.dao.impl.json.SingleShipmentBeanSerializer;
import com.visfresh.entities.AlertType;
import com.visfresh.entities.CorrectiveAction;
import com.visfresh.entities.DeviceModel;
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

import junit.framework.AssertionFailedError;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class SingleShipmentBeanSerializerTest {
    /**
     * Serializer to test.
     */
    private SingleShipmentBeanSerializer serializer;
    private static final ShortenerAliasesBuilder aliasesBuilder = new ShortenerAliasesBuilder();

    /**
     * Default constructor.
     */
    public SingleShipmentBeanSerializerTest() {
        super();
    }

    @Before
    public void setUp() {
        serializer = new SingleShipmentBeanSerializer();
//        serializer.setShortenerFactory(null);
    }
    @BeforeClass
    public static void beforeClass() {
        aliasesBuilder.setCollector((name, alias) -> {
            System.out.println("aliases.put(\""
                    + name
                    + "\", \""
                    + alias
                    + "\");");});
    }
    /**
     * Tests the bean serialization with default values. Should not throw any exceptions.
     */
    @Test
    public void testWithDefaultValues() {
        final SingleShipmentBean s = createBean();
        final JsonObject json = serializer.toJson(s);

        assertNotNull(json);
        assertNotNull(serializer.parseSingleShipmentBean(json));
    }
    @Test
    public void testSerializeMainValues() {
        final boolean alertsSuppressed = true;
        final Date alertsSuppressionTime = new Date(System.currentTimeMillis() - 100000111l);
        final Integer alertSuppressionMinutes = 34;
        final Integer arrivalNotificationWithinKm = 9;
        final boolean arrivalReportSent = true;
        final Date arrivalTime = new Date(System.currentTimeMillis() - 29487392587l);
        final String assetNum = "23872394857";
        final String assetType = "qpt8hkjn";
        final Integer batteryLevel = 3456;
        final String commentsForReceiver = "Any comments for receiver";
        final Long companyId = 9l;
        final Location currentLocation = new Location(12.13, 14.15);
        final String currentLocationDescription = "Undetermined location";
        final String c = "red";
        final String deviceName = "JUnit device";
        final String device = "2398709340827390847";
        final Date etaPretty = new Date(System.currentTimeMillis() - 9187023947908l);
        final boolean excludeNotificationsIfNoAlerts = true;
        final Date firstReadingTime = new Date(System.currentTimeMillis() - 19809870987l);
        final double temperature = 93.;
        final boolean latest = true;
        final double maxTemp = 45.5;
        final double minTemp = 6.6;
        final String nearestTracker = "2390817987908";
        final String nearestTrackerColor = "red";
        final Integer noAlertsAfterArrivalMinutes = 40;
        final Integer noAlertsAfterStartMinutes = 57;
        final String palletId = "palen N3";
        final int percentageComplete = 87;
        final boolean sendArrivalReport = true;
        final boolean sendArrivalReportOnlyIfAlerts = true;
        final String shipmentDescription = "JUnit shipment";
        final long shipmentId = 33l;
        final String shipmentType = "JUnit";
        final Integer shutDownAfterStartMinutes = 95;
        final Integer shutdownDeviceAfterMinutes = 67;
        final DeviceModel nearestTrackerModel = DeviceModel.STB1;
        final Date shutdownTime = new Date(System.currentTimeMillis() - 192837987987l);
        final Date startTime = new Date(System.currentTimeMillis() - 98709870987l);
        final ShipmentStatus status = ShipmentStatus.Ended;
        final int tripCount = 19;
        final DeviceModel model = DeviceModel.STB1;

        SingleShipmentBean s = createBean();

        s.setAlertsSuppressed(alertsSuppressed);
        s.setAlertsSuppressionTime(alertsSuppressionTime);
        s.setAlertSuppressionMinutes(alertSuppressionMinutes);
        s.setArrivalNotificationWithinKm(arrivalNotificationWithinKm);
        s.setArrivalReportSent(arrivalReportSent);
        s.setArrivalTime(arrivalTime);
        s.setAssetNum(assetNum);
        s.setAssetType(assetType);
        s.setBatteryLevel(batteryLevel);
        s.setCommentsForReceiver(commentsForReceiver);
        s.setCompanyId(companyId);
        s.setCurrentLocation(currentLocation);
        s.setCurrentLocationDescription(currentLocationDescription);
        s.setDeviceColor(c);
        s.setDeviceName(deviceName);
        s.setDevice(device);
        s.setDeviceModel(model);
        s.setEta(etaPretty);
        s.setExcludeNotificationsIfNoAlerts(excludeNotificationsIfNoAlerts);
        s.setFirstReadingTime(firstReadingTime);
        s.setLastReadingTemperature(temperature);
        s.setLatestShipment(latest);
        s.setMaxTemp(maxTemp);
        s.setMinTemp(minTemp);
        s.setNearestTracker(nearestTracker);
        s.setNearestTrackerColor(nearestTrackerColor);
        s.setNearestTrackerModel(nearestTrackerModel);
        s.setNoAlertsAfterArrivalMinutes(noAlertsAfterArrivalMinutes);
        s.setNoAlertsAfterStartMinutes(noAlertsAfterStartMinutes);
        s.setPalletId(palletId);
        s.setPercentageComplete(percentageComplete);
        s.setSendArrivalReport(sendArrivalReport);
        s.setSendArrivalReportOnlyIfAlerts(sendArrivalReportOnlyIfAlerts);
        s.setShipmentDescription(shipmentDescription);
        s.setShipmentId(shipmentId);
        s.setBeacon(true);
        s.setShipmentType(shipmentType);
        s.setShutDownAfterStartMinutes(shutDownAfterStartMinutes);
        s.setShutdownDeviceAfterMinutes(shutdownDeviceAfterMinutes);
        s.setShutdownTime(shutdownTime);
        s.setStartTime(startTime);
        s.setStatus(status);
        s.setTripCount(tripCount);

        s = jsonize(s);

        assertEquals(alertsSuppressed, s.isAlertsSuppressed());
        assertEqualDates(alertsSuppressionTime, s.getAlertsSuppressionTime());
        assertEquals(alertSuppressionMinutes, s.getAlertSuppressionMinutes());
        assertEquals(arrivalNotificationWithinKm, s.getArrivalNotificationWithinKm());
        assertEquals(arrivalReportSent, s.isArrivalReportSent());
        assertEqualDates(arrivalTime, s.getArrivalTime());
        assertEquals(assetNum, s.getAssetNum());
        assertEquals(assetType, s.getAssetType());
        assertEquals(batteryLevel, s.getBatteryLevel());
        assertEquals(commentsForReceiver, s.getCommentsForReceiver());
        assertEquals(companyId, s.getCompanyId());
        assertEquals(currentLocation.getLatitude(), s.getCurrentLocation().getLatitude(), 0.0001);
        assertEquals(currentLocation.getLongitude(), s.getCurrentLocation().getLongitude(), 0.0001);
        assertEquals(currentLocationDescription, s.getCurrentLocationDescription());
        assertEquals(c, s.getDeviceColor());
        assertEquals(deviceName, s.getDeviceName());
        assertEquals(device, s.getDevice());
        assertEquals(model, s.getDeviceModel());
        assertEqualDates(etaPretty, s.getEta());
        assertEquals(excludeNotificationsIfNoAlerts, s.isExcludeNotificationsIfNoAlerts());
        assertEqualDates(firstReadingTime, s.getFirstReadingTime());
        assertEquals(temperature, s.getLastReadingTemperature(), 0.0001);
        assertEquals(latest, s.isLatestShipment());
        assertEquals(maxTemp, s.getMaxTemp(), 0.0001);
        assertEquals(minTemp, s.getMinTemp(), 0.0001);
        assertEquals(nearestTracker, s.getNearestTracker());
        assertEquals(nearestTrackerColor, s.getNearestTrackerColor());
        assertEquals(nearestTrackerModel, s.getNearestTrackerModel());
        assertEquals(noAlertsAfterArrivalMinutes, s.getNoAlertsAfterArrivalMinutes());
        assertEquals(noAlertsAfterStartMinutes, s.getNoAlertsAfterStartMinutes());
        assertEquals(palletId, s.getPalletId());
        assertEquals(percentageComplete, s.getPercentageComplete());
        assertEquals(sendArrivalReport, s.isSendArrivalReport());
        assertEquals(sendArrivalReportOnlyIfAlerts, s.isSendArrivalReportOnlyIfAlerts());
        assertEquals(shipmentDescription, s.getShipmentDescription());
        assertEquals(shipmentId, s.getShipmentId());
        assertEquals(shipmentType, s.getShipmentType());
        assertEquals(true, s.isBeacon());
        assertEquals(shutDownAfterStartMinutes, s.getShutDownAfterStartMinutes());
        assertEquals(shutdownDeviceAfterMinutes, s.getShutdownDeviceAfterMinutes());
        assertEqualDates(shutdownTime, s.getShutdownTime());
        assertEqualDates(startTime, s.getStartTime());
        assertEquals(status, s.getStatus());
        assertEquals(tripCount, s.getTripCount());
    }
    /**
     * List<ListNotificationScheduleItem> alertsNotificationSchedules = new LinkedList<>();
     */
    @Test
    public void testAlertsNotificationSchedules() {
        final String notificationScheduleDescription = "Schedule description";
        final long notificationScheduleId = 57l;
        final String notificationScheduleName = "Schedule Name";
        final String peopleToNotify = "Too many peoples";

        ListNotificationScheduleItem sched = new ListNotificationScheduleItem();
        sched.setNotificationScheduleDescription(notificationScheduleDescription);
        sched.setNotificationScheduleId(notificationScheduleId);
        sched.setNotificationScheduleName(notificationScheduleName);
        sched.setPeopleToNotify(peopleToNotify);

        SingleShipmentBean s = createBean();
        s.getAlertsNotificationSchedules().add(sched);
        //add one empty only for check of count
        s.getAlertsNotificationSchedules().add(new ListNotificationScheduleItem());

        s = jsonize(s);

        assertEquals(2, s.getAlertsNotificationSchedules().size());

        sched = s.getAlertsNotificationSchedules().get(0);
        assertEquals(notificationScheduleDescription, sched.getNotificationScheduleDescription());
        assertEquals(notificationScheduleId, sched.getNotificationScheduleId());
        assertEquals(notificationScheduleName, sched.getNotificationScheduleName());
        assertEquals(peopleToNotify, sched.getPeopleToNotify());
    }
    @Test
    public void testSerializeArrival() {
        final Date date = new Date(System.currentTimeMillis() - 19823797l);
        final Long id = 14l;
        final Integer notifiedWhenKm = 90000;
        final Date notifiedAt = new Date(System.currentTimeMillis() - 2395798l);
        final Long trackerEventId = 8l;

        ArrivalBean arrival = new ArrivalBean();

        arrival.setDate(date);
        arrival.setId(id);
        arrival.setMettersForArrival(notifiedWhenKm);
        arrival.setNotifiedAt(notifiedAt);
        arrival.setTrackerEventId(trackerEventId);


        final SingleShipmentBean s = createBean();
        s.setArrival(arrival);

        arrival = jsonize(s).getArrival();

        assertEquals(date, arrival.getDate());
        assertEquals(id, arrival.getId());
        assertEquals(notifiedWhenKm, arrival.getMettersForArrival());
        assertEquals(notifiedAt, arrival.getNotifiedAt());
        assertEquals(trackerEventId, arrival.getTrackerEventId());
    }
    /**
     * List<ListNotificationScheduleItem> arrivalNotificationSchedules = new LinkedList<>();
     */
    @Test
    public void testArrivalNotificationSchedules() {
        final String notificationScheduleDescription = "Schedule description";
        final long notificationScheduleId = 57l;
        final String notificationScheduleName = "Schedule Name";
        final String peopleToNotify = "Too many peoples";

        ListNotificationScheduleItem sched = new ListNotificationScheduleItem();
        sched.setNotificationScheduleDescription(notificationScheduleDescription);
        sched.setNotificationScheduleId(notificationScheduleId);
        sched.setNotificationScheduleName(notificationScheduleName);
        sched.setPeopleToNotify(peopleToNotify);

        SingleShipmentBean s = createBean();
        s.getArrivalNotificationSchedules().add(sched);
        //add one empty only for check of count
        s.getArrivalNotificationSchedules().add(new ListNotificationScheduleItem());

        s = jsonize(s);

        assertEquals(2, s.getArrivalNotificationSchedules().size());

        sched = s.getArrivalNotificationSchedules().get(0);
        assertEquals(notificationScheduleDescription, sched.getNotificationScheduleDescription());
        assertEquals(notificationScheduleId, sched.getNotificationScheduleId());
        assertEquals(notificationScheduleName, sched.getNotificationScheduleName());
        assertEquals(peopleToNotify, sched.getPeopleToNotify());
    }
    /**
     * LocationProfileBean startLocation;
     */
    @Test
    public void testStartLocation() {
        final String address = "Odessa, Derebasovskaya st.";
        final String company = "SmartTrace LLC";
        final Long id = 7l;
        final boolean interim = true;
        final String name = "Location name";
        final String notes = "Location notest";
        final int radius = 957;
        final boolean start = true;
        final boolean stop = true;

        LocationProfileBean loc = new LocationProfileBean();
        loc.setAddress(address);
        loc.setCompanyName(company);
        loc.setId(id);
        loc.setInterim(interim);
        loc.setName(name);
        loc.setNotes(notes);
        loc.setRadius(radius);
        loc.setStart(start);
        loc.setStop(stop);

        final SingleShipmentBean s = createBean();
        s.setStartLocation(loc);

        loc = jsonize(s).getStartLocation();

        assertNotNull(loc);

        assertEquals(address, loc.getAddress());
        assertEquals(company, loc.getCompanyName());
        assertEquals(id, loc.getId());
        assertEquals(interim, loc.isInterim());
        assertEquals(name, loc.getName());
        assertEquals(notes, loc.getNotes());
        assertEquals(radius, loc.getRadius());
        assertEquals(start, loc.isStart());
        assertEquals(stop, loc.isStop());
    }
    /**
     * LocationProfileBean endLocation;
     */
    @Test
    public void testEndLocation() {
        final String address = "Odessa, Derebasovskaya st.";
        final String company = "SmartTrace LLC";
        final Long id = 7l;
        final boolean interim = true;
        final String name = "Location name";
        final String notes = "Location notest";
        final int radius = 957;
        final boolean start = true;
        final boolean stop = true;

        LocationProfileBean loc = new LocationProfileBean();
        loc.setAddress(address);
        loc.setCompanyName(company);
        loc.setId(id);
        loc.setInterim(interim);
        loc.setName(name);
        loc.setNotes(notes);
        loc.setRadius(radius);
        loc.setStart(start);
        loc.setStop(stop);

        final SingleShipmentBean s = createBean();
        s.setEndLocation(loc);

        loc = jsonize(s).getEndLocation();

        assertNotNull(loc);

        assertEquals(address, loc.getAddress());
        assertEquals(company, loc.getCompanyName());
        assertEquals(id, loc.getId());
        assertEquals(interim, loc.isInterim());
        assertEquals(name, loc.getName());
        assertEquals(notes, loc.getNotes());
        assertEquals(radius, loc.getRadius());
        assertEquals(start, loc.isStart());
        assertEquals(stop, loc.isStop());
    }
    /**
     * List<SingleShipmentLocationBean> locations = new LinkedList<>();
     */
    @Test
    public void testLocations() {
        final Long id = 56l;
        final Double latitude = 7.7;
        final Double longitude = 8.8;
        final double temperature = 34.45;
        final int humidity = 77;
        final Date time = new Date(System.currentTimeMillis() - 1089847l);
        final TrackerEventType eventType = TrackerEventType.AUT;

        SingleShipmentLocationBean loc = new SingleShipmentLocationBean();
        loc.setId(id);
        loc.setLatitude(latitude);
        loc.setLongitude(longitude);
        loc.setTemperature(temperature);
        loc.setTime(time);
        loc.setType(eventType);
        loc.setHumidity(humidity);

        loc = serializer.parseSingleShipmentLocationBean(serializer.toJson(loc));

        assertEquals(id, loc.getId());
        assertEquals(latitude, loc.getLatitude(), 0.001);
        assertEquals(longitude, loc.getLongitude(), 0.001);
        assertEquals(temperature, loc.getTemperature(), 0.001);
        assertEquals(time, loc.getTime());
        assertEquals(eventType, loc.getType());
        assertEquals(humidity, loc.getHumidity().intValue());
    }
    /**
     * List<SingleShipmentLocationBean> locations = new LinkedList<>();
     */
    @Test
    public void testLocationsAlerts() {
        final Date date = new Date(System.currentTimeMillis() - 9234709l);
        final Long id = 99l;
        final Long trackerEventId = 89l;
        final AlertType type = AlertType.Battery;

        AlertBean a = new AlertBean();
        a.setDate(date);
        a.setId(id);
        a.setTrackerEventId(trackerEventId);
        a.setType(type);

        SingleShipmentLocationBean loc = new SingleShipmentLocationBean();
        loc.setType(TrackerEventType.AUT);
        loc.getAlerts().add(a);
        loc.getAlerts().add(a);

        loc = serializer.parseSingleShipmentLocationBean(serializer.toJson(loc));

        a = loc.getAlerts().get(0);

        assertFalse(a instanceof TemperatureAlertBean);
        assertEqualDates(date, a.getDate());
        assertEquals(id, a.getId());
        assertEquals(trackerEventId, a.getTrackerEventId());
        assertEquals(type, a.getType());
    }
    /**
     * List<SingleShipmentLocationBean> locations = new LinkedList<>();
     */
    @Test
    public void testLocationsTemparatureAlerts() {
        final Date date = new Date(System.currentTimeMillis() - 9234709l);
        final Long id = 99l;
        final Long trackerEventId = 89l;
        final AlertType type = AlertType.Hot;
        final boolean cumulative = true;
        final int minutes = 456;
        final Long ruleId = 87l;
        final double temperature = 36.6;

        TemperatureAlertBean a = new TemperatureAlertBean();
        a.setDate(date);
        a.setId(id);
        a.setTrackerEventId(trackerEventId);
        a.setType(type);
        a.setCumulative(cumulative);
        a.setMinutes(minutes);
        a.setRuleId(ruleId);
        a.setTemperature(temperature);

        SingleShipmentLocationBean loc = new SingleShipmentLocationBean();
        loc.setType(TrackerEventType.AUT);
        loc.getAlerts().add(a);

        loc = serializer.parseSingleShipmentLocationBean(serializer.toJson(loc));
        a = (TemperatureAlertBean) loc.getAlerts().get(0);

        assertEqualDates(date, a.getDate());
        assertEquals(id, a.getId());
        assertEquals(trackerEventId, a.getTrackerEventId());
        assertEquals(type, a.getType());
        assertEquals(cumulative, a.isCumulative());
        assertEquals(minutes, a.getMinutes());
        assertEquals(ruleId, a.getRuleId());
        assertEquals(temperature, a.getTemperature(), 0.0001);

    }
    /**
     * List<Long> siblings = new LinkedList<>();
     */
    @Test
    public void testSiblings() {
        SingleShipmentBean s = createBean();

        s.getSiblings().add(1l);
        s.getSiblings().add(2l);

        s = jsonize(s);

        assertEquals(2, s.getSiblings().size());
        assertEquals(new Long(1), s.getSiblings().get(0));
        assertEquals(new Long(2), s.getSiblings().get(1));
    }
    /**
     * List<AlertRuleBean> alertYetToFire = new LinkedList<>();
     */
    @Test
    public void testAlertYetToFire() {
        final Long id = 11l;
        final AlertType type = AlertType.Battery;

        AlertRuleBean a = new AlertRuleBean();
        a.setId(id);
        a.setType(type);

        final SingleShipmentBean s = createBean();
        s.getAlertYetToFire().add(a);
        s.getAlertYetToFire().add(a);

        assertEquals(2, s.getAlertYetToFire().size());

        a = s.getAlertYetToFire().get(0);

        assertEquals(id, a.getId());
        assertEquals(type, a.getType());
        assertFalse(a instanceof TemperatureRuleBean);
    }
    /**
     * List<AlertRuleBean> alertYetToFire = new LinkedList<>();
     */
    @Test
    public void testAlertYetToFireTemperatureRule() {
        final Long id = 11l;
        final AlertType type = AlertType.CriticalCold;
        final boolean cumulativeFlag = true;
        final Integer maxRateMinutes = 17;
        final int timeOutMinutes = 54;
        final double temperature = 55.5;

        TemperatureRuleBean a = new TemperatureRuleBean();
        a.setId(id);
        a.setType(type);
        a.setCumulativeFlag(cumulativeFlag);
        a.setMaxRateMinutes(maxRateMinutes);
        a.setTimeOutMinutes(timeOutMinutes);
        a.setTemperature(temperature);

        final SingleShipmentBean s = createBean();
        s.getAlertYetToFire().add(a);

        assertEquals(1, s.getAlertYetToFire().size());

        a = (TemperatureRuleBean) s.getAlertYetToFire().get(0);

        assertEquals(id, a.getId());
        assertEquals(type, a.getType());
        assertEquals(cumulativeFlag, a.hasCumulativeFlag());
        assertEquals(maxRateMinutes, a.getMaxRateMinutes());
        assertEquals(timeOutMinutes, a.getTimeOutMinutes());
        assertEquals(temperature, a.getTemperature(), 0.0001);
    }
    @Test
    public void testAlertYetToFireCorrectiveActions() {
        final String description = "Actions description";
        final Long id = 7l;
        final String name = "Action list name";

        CorrectiveActionListBean a = new CorrectiveActionListBean();
        a.setDescription(description);
        a.setId(id);
        a.setName(name);
        a.getActions().add(new CorrectiveAction("A", true));
        a.getActions().add(new CorrectiveAction("B", false));

        final TemperatureRuleBean tr = new TemperatureRuleBean();
        tr.setId(1l);
        tr.setType(AlertType.Cold);
        tr.setCorrectiveActions(a);

        final SingleShipmentBean s = createBean();
        s.getAlertYetToFire().add(tr);

        a = ((TemperatureRuleBean) s.getAlertYetToFire().get(0)).getCorrectiveActions();

        assertEquals(description, a.getDescription());
        assertEquals(id, a.getId());
        assertEquals(name, a.getName());
        assertEquals("A", a.getActions().get(0).getAction());
        assertEquals(true, a.getActions().get(0).isRequestVerification());
        assertEquals("B", a.getActions().get(1).getAction());
        assertEquals(false, a.getActions().get(1).isRequestVerification());
    }
    /**
     * List<AlertRuleBean> alertFired = new LinkedList<>();
     */
    @Test
    public void testAlertFired() {
        final Long id = 11l;
        final AlertType type = AlertType.Battery;

        AlertRuleBean a = new AlertRuleBean();
        a.setId(id);
        a.setType(type);

        final SingleShipmentBean s = createBean();
        s.getAlertFired().add(a);
        s.getAlertFired().add(a);

        assertEquals(2, s.getAlertFired().size());

        a = s.getAlertFired().get(0);

        assertEquals(id, a.getId());
        assertEquals(type, a.getType());
        assertFalse(a instanceof TemperatureRuleBean);
    }
    /**
     * List<AlertRuleBean> alertFired = new LinkedList<>();
     */
    @Test
    public void testAlertFiredTemperatureRule() {
        final Long id = 11l;
        final AlertType type = AlertType.CriticalCold;
        final boolean cumulativeFlag = true;
        final Integer maxRateMinutes = 17;
        final int timeOutMinutes = 54;
        final double temperature = 55.5;

        TemperatureRuleBean a = new TemperatureRuleBean();
        a.setId(id);
        a.setType(type);
        a.setCumulativeFlag(cumulativeFlag);
        a.setMaxRateMinutes(maxRateMinutes);
        a.setTimeOutMinutes(timeOutMinutes);
        a.setTemperature(temperature);

        final SingleShipmentBean s = createBean();
        s.getAlertFired().add(a);

        assertEquals(1, s.getAlertFired().size());

        a = (TemperatureRuleBean) s.getAlertFired().get(0);

        assertEquals(id, a.getId());
        assertEquals(type, a.getType());
        assertEquals(cumulativeFlag, a.hasCumulativeFlag());
        assertEquals(maxRateMinutes, a.getMaxRateMinutes());
        assertEquals(timeOutMinutes, a.getTimeOutMinutes());
        assertEquals(temperature, a.getTemperature(), 0.0001);
    }
    /**
     * List<AlertRuleBean> alertFired = new LinkedList<>();
     */
    @Test
    public void testAlertFiredCorrectiveActions() {
        final String description = "Actions description";
        final Long id = 7l;
        final String name = "Action list name";

        CorrectiveActionListBean a = new CorrectiveActionListBean();
        a.setDescription(description);
        a.setId(id);
        a.setName(name);
        a.getActions().add(new CorrectiveAction("A", true));
        a.getActions().add(new CorrectiveAction("B", false));

        final TemperatureRuleBean tr = new TemperatureRuleBean();
        tr.setId(1l);
        tr.setType(AlertType.Cold);
        tr.setCorrectiveActions(a);

        final SingleShipmentBean s = createBean();
        s.getAlertFired().add(tr);

        a = ((TemperatureRuleBean) s.getAlertFired().get(0)).getCorrectiveActions();

        assertEquals(description, a.getDescription());
        assertEquals(id, a.getId());
        assertEquals(name, a.getName());
        assertEquals("A", a.getActions().get(0).getAction());
        assertEquals(true, a.getActions().get(0).isRequestVerification());
        assertEquals("B", a.getActions().get(1).getAction());
        assertEquals(false, a.getActions().get(1).isRequestVerification());
    }
    /**
     * List<LocationProfileBean> startLocationAlternatives = new LinkedList<>();
     */
    @Test
    public void testStartLocationAlternatives() {
        final String address = "Odessa, Derivasovskaya st.";
        final String company = "Company name";
        final Long id = 2l;
        final boolean interim = true;
        final String name = "Locaton name";
        final String notes = "Location notest";
        final int radius = 555;
        final boolean start = true;
        final boolean stop = true;

        LocationProfileBean loc = new LocationProfileBean();
        loc.setAddress(address);
        loc.setCompanyName(company);
        loc.setId(id);
        loc.setInterim(interim);
        loc.setName(name);
        loc.setNotes(notes);
        loc.setRadius(radius);
        loc.setStart(start);
        loc.setStop(stop);

        SingleShipmentBean s = createBean();

        s.getStartLocationAlternatives().add(loc);
        s.getStartLocationAlternatives().add(loc);

        s = jsonize(s);

        assertEquals(2, s.getStartLocationAlternatives().size());

        loc = s.getStartLocationAlternatives().get(0);

        assertEquals(address, loc.getAddress());
        assertEquals(company, loc.getCompanyName());
        assertEquals(id, loc.getId());
        assertEquals(interim, loc.isInterim());
        assertEquals(name, loc.getName());
        assertEquals(notes, loc.getNotes());
        assertEquals(radius, loc.getRadius());
        assertEquals(start, loc.isStart());
        assertEquals(stop, loc.isStop());
    }
    /**
     * List<LocationProfileBean> endLocationAlternatives = new LinkedList<>();
     */
    @Test
    public void testEndLocationAlternatives() {
        final String address = "Odessa, Derivasovskaya st.";
        final String company = "Company name";
        final Long id = 2l;
        final boolean interim = true;
        final String name = "Locaton name";
        final String notes = "Location notest";
        final int radius = 555;
        final boolean start = true;
        final boolean stop = true;

        LocationProfileBean loc = new LocationProfileBean();
        loc.setAddress(address);
        loc.setCompanyName(company);
        loc.setId(id);
        loc.setInterim(interim);
        loc.setName(name);
        loc.setNotes(notes);
        loc.setRadius(radius);
        loc.setStart(start);
        loc.setStop(stop);

        SingleShipmentBean s = createBean();

        s.getEndLocationAlternatives().add(loc);
        s.getEndLocationAlternatives().add(loc);

        s = jsonize(s);

        assertEquals(2, s.getEndLocationAlternatives().size());

        loc = s.getEndLocationAlternatives().get(0);

        assertEquals(address, loc.getAddress());
        assertEquals(company, loc.getCompanyName());
        assertEquals(id, loc.getId());
        assertEquals(interim, loc.isInterim());
        assertEquals(name, loc.getName());
        assertEquals(notes, loc.getNotes());
        assertEquals(radius, loc.getRadius());
        assertEquals(start, loc.isStart());
        assertEquals(stop, loc.isStop());
    }
    /**
     * List<LocationProfileBean> interimLocationAlternatives = new LinkedList<>();
     */
    @Test
    public void testInterimLocationAlternatives() {
        final String address = "Odessa, Derivasovskaya st.";
        final String company = "Company name";
        final Long id = 2l;
        final boolean interim = true;
        final String name = "Locaton name";
        final String notes = "Location notest";
        final int radius = 555;
        final boolean start = true;
        final boolean stop = true;

        LocationProfileBean loc = new LocationProfileBean();
        loc.setAddress(address);
        loc.setCompanyName(company);
        loc.setId(id);
        loc.setInterim(interim);
        loc.setName(name);
        loc.setNotes(notes);
        loc.setRadius(radius);
        loc.setStart(start);
        loc.setStop(stop);

        SingleShipmentBean s = createBean();

        s.getInterimLocationAlternatives().add(loc);
        s.getInterimLocationAlternatives().add(loc);

        s = jsonize(s);

        assertEquals(2, s.getInterimLocationAlternatives().size());

        loc = s.getInterimLocationAlternatives().get(0);

        assertEquals(address, loc.getAddress());
        assertEquals(company, loc.getCompanyName());
        assertEquals(id, loc.getId());
        assertEquals(interim, loc.isInterim());
        assertEquals(name, loc.getName());
        assertEquals(notes, loc.getNotes());
        assertEquals(radius, loc.getRadius());
        assertEquals(start, loc.isStart());
        assertEquals(stop, loc.isStop());
    }
    /**
     * List<InterimStopBean> interimStops = new LinkedList<>();
     */
    @Test
    public void testInterimStops() {
        //location
        final String address = "Odessa, Derivasovskaya st.";
        final String company = "Company name";
        final Long id = 2l;
        final boolean interim = true;
        final String name = "Locaton name";
        final String notes = "Location notest";
        final int radius = 555;
        final boolean start = true;
        final boolean stop = true;

        LocationProfileBean loc = new LocationProfileBean();
        loc.setAddress(address);
        loc.setCompanyName(company);
        loc.setId(id);
        loc.setInterim(interim);
        loc.setName(name);
        loc.setNotes(notes);
        loc.setRadius(radius);
        loc.setStart(start);
        loc.setStop(stop);

        //interim stop
        final Long stpId = 45l;
        final Date stopDate = new Date(System.currentTimeMillis() - 1000098098l);
        final int time = 15;

        InterimStopBean stp = new InterimStopBean();
        stp.setId(stpId);
        stp.setLocation(loc);
        stp.setStopDate(stopDate);
        stp.setTime(time);

        SingleShipmentBean s = createBean();

        s.getInterimStops().add(stp);
        s.getInterimStops().add(stp);

        s = jsonize(s);
        assertEquals(2, s.getInterimStops().size());

        //interim stop
        stp = s.getInterimStops().get(0);
        assertEquals(stpId, stp.getId());
        assertNotNull(stp.getLocation());
        assertEqualDates(stopDate, stp.getStopDate());
        assertEquals(time, stp.getTime());

        //location
        loc = stp.getLocation();

        assertEquals(address, loc.getAddress());
        assertEquals(company, loc.getCompanyName());
        assertEquals(id, loc.getId());
        assertEquals(interim, loc.isInterim());
        assertEquals(name, loc.getName());
        assertEquals(notes, loc.getNotes());
        assertEquals(radius, loc.getRadius());
        assertEquals(start, loc.isStart());
        assertEquals(stop, loc.isStop());
    }
    /**
     * List<NoteDto> notes = new LinkedList<>();
     */
    @Test
    public void testNotes() {
        final boolean active = true;
        final String createCreatedByName = "Created By Name";
        final String createdBy = "Created By";
        final Date creationDate = new Date(System.currentTimeMillis() - 1923470987l);
        final Integer noteNum = 57;
        final String noteText = "Note Text";
        final String noteType = "Note type";
        final Date timeOnChart = new Date(System.currentTimeMillis() - 129087009l);

        NoteBean note = new NoteBean();
        note.setActive(active);
        note.setCreatedByName(createCreatedByName);
        note.setCreatedBy(createdBy);
        note.setCreationDate(creationDate);
        note.setNoteNum(noteNum);
        note.setNoteText(noteText);
        note.setNoteType(noteType);
        note.setTimeOnChart(timeOnChart);

        SingleShipmentBean s = createBean();

        s.getNotes().add(note);
        s.getNotes().add(note);

        s = jsonize(s);

        assertEquals(2, s.getNotes().size());

        note = s.getNotes().get(0);

        assertEquals(active, note.isActive());
        assertEquals(createCreatedByName, note.getCreatedByName());
        assertEquals(createdBy, note.getCreatedBy());
        assertEqualDates(creationDate, note.getCreationDate());
        assertEquals(noteNum, note.getNoteNum());
        assertEquals(noteText, note.getNoteText());
        assertEquals(noteType, note.getNoteType());
        assertEqualDates(timeOnChart, note.getTimeOnChart());
    }
    /**
     * List<DeviceGroupDto> deviceGroups = new LinkedList<>();
     */
    @Test
    public void testDeviceGroups() {
        final String description = "Device group description";
        final Long id = 19l;
        final String name = "Device Group Name";

        DeviceGroupDto group = new DeviceGroupDto();
        group.setDescription(description);
        group.setId(id);
        group.setName(name);

        SingleShipmentBean s = createBean();

        s.getDeviceGroups().add(group);
        s.getDeviceGroups().add(group);

        s = jsonize(s);

        assertEquals(2, s.getDeviceGroups().size());
        group = s.getDeviceGroups().get(0);

        assertEquals(description, group.getDescription());
        assertEquals(id, group.getId());
        assertEquals(name, group.getName());
    }
    /**
     * List<ShipmentUserDto> userAccess = new LinkedList<>();
     */
    @Test
    public void testUserAccess() {
        final String email = "junit@smarttrace.com.au";
        final Long id = 91l;

        ShipmentUserDto u = new ShipmentUserDto();
        u.setEmail(email);
        u.setId(id);

        SingleShipmentBean s = createBean();

        s.getUserAccess().add(u);
        s.getUserAccess().add(u);

        s = jsonize(s);

        assertEquals(2, s.getUserAccess().size());
        u = s.getUserAccess().get(0);

        assertEquals(email, u.getEmail());
        assertEquals(id, u.getId());
    }
    /**
     * List<ShipmentCompanyDto> companyAccess = new LinkedList<>();
     */
    @Test
    public void testCompanyAccess() {
        final Long id = 34l;
        final String name = "Company Name";

        ShipmentCompanyDto c = new ShipmentCompanyDto();
        c.setId(id);
        c.setName(name);

        SingleShipmentBean s = createBean();

        s.getCompanyAccess().add(c);
        s.getCompanyAccess().add(c);

        s = jsonize(s);

        assertEquals(2, s.getCompanyAccess().size());
        c = s.getCompanyAccess().get(0);

        assertEquals(id, c.getId());
        assertEquals(name, c.getName());
    }
    /**
     * List<AlertBean> sentAlerts = new LinkedList<>();
     */
    @Test
    public void testSentAlerts() {
        final Date date = new Date(System.currentTimeMillis() - 9234709l);
        final Long id = 99l;
        final Long trackerEventId = 89l;
        final AlertType type = AlertType.Battery;

        AlertBean a = new AlertBean();
        a.setDate(date);
        a.setId(id);
        a.setTrackerEventId(trackerEventId);
        a.setType(type);

        SingleShipmentBean s = createBean();
        s.getSentAlerts().add(a);
        s.getSentAlerts().add(a);

        s = jsonize(s);

        assertEquals(2, s.getSentAlerts().size());
        a = s.getSentAlerts().get(0);

        assertFalse(a instanceof TemperatureAlertBean);
        assertEqualDates(date, a.getDate());
        assertEquals(id, a.getId());
        assertEquals(trackerEventId, a.getTrackerEventId());
        assertEquals(type, a.getType());
    }
    /**
     * List<AlertBean> sentAlerts = new LinkedList<>();
     */
    @Test
    public void testSentTemperatureAlerts() {
        final Date date = new Date(System.currentTimeMillis() - 9234709l);
        final Long id = 99l;
        final Long trackerEventId = 89l;
        final AlertType type = AlertType.Hot;
        final boolean cumulative = true;
        final int minutes = 456;
        final Long ruleId = 87l;
        final double temperature = 36.6;

        TemperatureAlertBean a = new TemperatureAlertBean();
        a.setDate(date);
        a.setId(id);
        a.setTrackerEventId(trackerEventId);
        a.setType(type);
        a.setCumulative(cumulative);
        a.setMinutes(minutes);
        a.setRuleId(ruleId);
        a.setTemperature(temperature);

        SingleShipmentBean s = createBean();
        s.getSentAlerts().add(a);

        s = jsonize(s);

        a = (TemperatureAlertBean) s.getSentAlerts().get(0);

        assertEqualDates(date, a.getDate());
        assertEquals(id, a.getId());
        assertEquals(trackerEventId, a.getTrackerEventId());
        assertEquals(type, a.getType());
        assertEquals(cumulative, a.isCumulative());
        assertEquals(minutes, a.getMinutes());
        assertEquals(ruleId, a.getRuleId());
        assertEquals(temperature, a.getTemperature(), 0.0001);
    }
    /**
     * AlertProfileDto alertProfile;
     */
    @Test
    public void testAlertProfile() {
        final String description = "Alert Profile description";
        final Long id = 35l;
        final double lowerTemperatureLimit = 34.4;
        final String name = "Alert Profile Name";
        final double upperTemperatureLimit = 56.1;
        final boolean watchBatteryLow = true;
        final boolean watchEnterBrightEnvironment = true;
        final boolean watchEnterDarkEnvironment = true;
        final boolean watchMovementStart = true;
        final boolean watchMovementStop = true;

        AlertProfileBean ap = new AlertProfileBean();
        ap.setDescription(description);
        ap.setId(id);
        ap.setLowerTemperatureLimit(lowerTemperatureLimit);
        ap.setName(name);
        ap.setUpperTemperatureLimit(upperTemperatureLimit);
        ap.setWatchBatteryLow(watchBatteryLow);
        ap.setWatchEnterBrightEnvironment(watchEnterBrightEnvironment);
        ap.setWatchEnterDarkEnvironment(watchEnterDarkEnvironment);
        ap.setWatchMovementStart(watchMovementStart);
        ap.setWatchMovementStop(watchMovementStop);

        final SingleShipmentBean s = createBean();
        s.setAlertProfile(ap);

        ap = jsonize(s).getAlertProfile();
        assertEquals(description, ap.getDescription());
        assertEquals(id, ap.getId());
        assertEquals(lowerTemperatureLimit, ap.getLowerTemperatureLimit(), 0.0001);
        assertEquals(name, ap.getName());
        assertEquals(upperTemperatureLimit, ap.getUpperTemperatureLimit(), 0.0001);
        assertEquals(watchBatteryLow, ap.isWatchBatteryLow());
        assertEquals(watchEnterBrightEnvironment, ap.isWatchEnterBrightEnvironment());
        assertEquals(watchEnterDarkEnvironment, ap.isWatchEnterDarkEnvironment());
        assertEquals(watchMovementStart, ap.isWatchMovementStart());
        assertEquals(watchMovementStop, ap.isWatchMovementStop());
    }
    /**
     * AlertProfileDto alertProfile;
     */
    @Test
    public void testNullAlertProfile() {
        final SingleShipmentBean s = createBean();
        s.setAlertProfile(null);

        assertNull(jsonize(s).getAlertProfile());
    }
    /**
     * AlertProfileDto alertProfile;
     */
    @Test
    public void testExportToViewDataNullAlertProfile() {
        final SingleShipmentBean s = createBean();
        s.setAlertProfile(null);

        //check not NullPointerException
        final SingleShipmentData data = new SingleShipmentData();
        data.setBean(s);
        serializer.exportToViewData(data);
    }
    @Test
    public void testAlertProfileLightOnCorrectiveAction() {
        final String description = "Actions description";
        final Long id = 7l;
        final String name = "Action list name";

        CorrectiveActionListBean a = new CorrectiveActionListBean();
        a.setDescription(description);
        a.setId(id);
        a.setName(name);
        a.getActions().add(new CorrectiveAction("A", true));
        a.getActions().add(new CorrectiveAction("B", false));

        final AlertProfileBean ap = new AlertProfileBean();
        ap.setLightOnCorrectiveActions(a);

        final SingleShipmentBean s = createBean();
        s.setAlertProfile(ap);

        a = jsonize(s).getAlertProfile().getLightOnCorrectiveActions();

        assertEquals(description, a.getDescription());
        assertEquals(id, a.getId());
        assertEquals(name, a.getName());
        assertEquals("A", a.getActions().get(0).getAction());
        assertEquals(true, a.getActions().get(0).isRequestVerification());
        assertEquals("B", a.getActions().get(1).getAction());
        assertEquals(false, a.getActions().get(1).isRequestVerification());
    }
    @Test
    public void testAlertProfileBatteryLowCorrectiveAction() {
        final String description = "Actions description";
        final Long id = 7l;
        final String name = "Action list name";

        CorrectiveActionListBean a = new CorrectiveActionListBean();
        a.setDescription(description);
        a.setId(id);
        a.setName(name);
        a.getActions().add(new CorrectiveAction("A", true));
        a.getActions().add(new CorrectiveAction("B", false));

        final AlertProfileBean ap = new AlertProfileBean();
        ap.setBatteryLowCorrectiveActions(a);

        final SingleShipmentBean s = createBean();
        s.setAlertProfile(ap);

        a = jsonize(s).getAlertProfile().getBatteryLowCorrectiveActions();

        assertEquals(description, a.getDescription());
        assertEquals(id, a.getId());
        assertEquals(name, a.getName());
        assertEquals("A", a.getActions().get(0).getAction());
        assertEquals(true, a.getActions().get(0).isRequestVerification());
        assertEquals("B", a.getActions().get(1).getAction());
        assertEquals(false, a.getActions().get(1).isRequestVerification());
    }

    /**
     * @param s
     * @return
     */
    private SingleShipmentBean jsonize(final SingleShipmentBean s) {
        final JsonObject json = serializer.toJson(s);
        aliasesBuilder.addProperiesFromJson(json);
        return serializer.parseSingleShipmentBean(json);
    }

    private void assertEqualDates(final Date d1, final Date d2) throws AssertionFailedError {
        assertTrue(Math.abs(d1.getTime() - d2.getTime()) < 100);
    }
    /**
     * @return empty bean with only status set.
     */
    protected SingleShipmentBean createBean() {
        final SingleShipmentBean s = new SingleShipmentBean();
        //status is mandatory value
        s.setStatus(ShipmentStatus.Arrived);
        return s;
    }
}
