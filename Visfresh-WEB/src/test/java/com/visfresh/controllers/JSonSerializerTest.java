/**
 *
 */
package com.visfresh.controllers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Date;

import org.junit.Before;
import org.junit.Test;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.visfresh.entities.Alert;
import com.visfresh.entities.AlertProfile;
import com.visfresh.entities.AlertType;
import com.visfresh.entities.Arrival;
import com.visfresh.entities.Company;
import com.visfresh.entities.Device;
import com.visfresh.entities.DeviceCommand;
import com.visfresh.entities.DeviceData;
import com.visfresh.entities.LocationProfile;
import com.visfresh.entities.Notification;
import com.visfresh.entities.NotificationSchedule;
import com.visfresh.entities.NotificationType;
import com.visfresh.entities.PersonalSchedule;
import com.visfresh.entities.Role;
import com.visfresh.entities.Shipment;
import com.visfresh.entities.ShipmentData;
import com.visfresh.entities.ShipmentStatus;
import com.visfresh.entities.ShipmentTemplate;
import com.visfresh.entities.TemperatureAlert;
import com.visfresh.entities.TrackerEvent;
import com.visfresh.entities.User;
import com.visfresh.entities.UserProfile;
import com.visfresh.io.CreateUserRequest;
import com.visfresh.io.JSonSerializer;
import com.visfresh.io.SaveShipmentRequest;
import com.visfresh.io.SaveShipmentResponse;
import com.visfresh.mpl.services.DeviceDcsNativeEvent;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class JSonSerializerTest {
    /**
     * Factory to test.
     */
    private JSonSerializer serializer;
    private MockReferenceResolver resolver;
    private long lastLong;
    private Gson gson;

    /**
     * default constructor.
     */
    public JSonSerializerTest() {
        super();
    }

    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    @Before
    public void setUp() throws Exception {
        final GsonBuilder b = new GsonBuilder();
        b.setPrettyPrinting();
        this.gson = b.create();

        resolver = new MockReferenceResolver();
        serializer = new JSonSerializer();
        serializer.setReferenceResolver(resolver);
    }

    @Test
    public void testAlertProfile() {
        final double criticalHighTemperature = 5.77;
        final double criticalLowTemperature = -15.88;
        final String description = "Any description";
        final double highTemperature = 1.22;
        final int highTemperatureForMoreThen = 55; // min
        final Long id = 7L;
        final double lowTemperature = -10.33;
        final int lowTemperatureForMoreThen = 55; //min
        final String name = "AnyName";
        final boolean watchBatteryLow = true;
        final boolean watchEnterBrightEnvironment = true;
        final boolean watchEnterDarkEnvironment = false;
        final boolean watchShock = false;
        final int criticalHighTemperatureForMoreThen = 91;
        final int criticalLowTemperatureForMoreThen = 71;

        AlertProfile p = new AlertProfile();
        p.setCriticalHighTemperature(criticalHighTemperature);
        p.setCriticalLowTemperature(criticalLowTemperature);
        p.setDescription(description);
        p.setHighTemperature(highTemperature);
        p.setHighTemperatureForMoreThen(highTemperatureForMoreThen);
        p.setId(id);
        p.setLowTemperature(lowTemperature);
        p.setLowTemperatureForMoreThen(lowTemperatureForMoreThen);
        p.setName(name);
        p.setWatchBatteryLow(watchBatteryLow);
        p.setWatchEnterBrightEnvironment(watchEnterBrightEnvironment);
        p.setWatchEnterDarkEnvironment(watchEnterDarkEnvironment);
        p.setWatchShock(watchShock);
        p.setCriticalHighTemperatureForMoreThen(criticalHighTemperatureForMoreThen);
        p.setCriticalLowTemperatureForMoreThen(criticalLowTemperatureForMoreThen);

        final JsonObject json = serializer.toJson(p).getAsJsonObject();
        p = serializer.parseAlertProfile(json);

        assertEquals(criticalHighTemperature, p.getCriticalHighTemperature(), 0.00001);
        assertEquals(criticalLowTemperature, p.getCriticalLowTemperature(), 0.00001);
        assertEquals(description, p.getDescription());
        assertEquals(highTemperature, p.getHighTemperature(), 0.00001);
        assertEquals(highTemperatureForMoreThen, p.getHighTemperatureForMoreThen());
        assertEquals(id, p.getId());
        assertEquals(lowTemperature, p.getLowTemperature(), 0.00001);
        assertEquals(lowTemperatureForMoreThen, p.getLowTemperatureForMoreThen());
        assertEquals(name, p.getName());
        assertEquals(watchBatteryLow, p.isWatchBatteryLow());
        assertEquals(watchEnterBrightEnvironment, p.isWatchEnterBrightEnvironment());
        assertEquals(watchEnterDarkEnvironment, p.isWatchEnterDarkEnvironment());
        assertEquals(watchShock, p.isWatchShock());
        assertEquals(criticalHighTemperatureForMoreThen, p.getCriticalHighTemperatureForMoreThen());
        assertEquals(criticalLowTemperatureForMoreThen, p.getCriticalLowTemperatureForMoreThen());
    }
    @Test
    public void testSchedulePersonHowWhen() {
        PersonalSchedule s = new PersonalSchedule();

        final String company = "Sun";
        final String emailNotification = "anybody@sun.com";
        final String firstName = "Alexander";
        final int forMinute = 17;
        final int fromMinute = 1;
        final Long id = 77l;
        final String lastName = "Suvorov";
        final String position = "General";
        final boolean pushToMobileApp = true;
        final String smsNotification = "1111111117";

        s.setCompany(company);
        s.setEmailNotification(emailNotification);
        s.setFirstName(firstName);
        s.setToTime(forMinute);
        s.setFromTime(fromMinute);
        s.setId(id);
        s.setLastName(lastName);
        s.setPosition(position);
        s.setPushToMobileApp(pushToMobileApp);
        s.setSmsNotification(smsNotification);
        s.getWeekDays()[0] = true;
        s.getWeekDays()[3] = true;

        final JsonObject obj = serializer.toJson(s);
        s = serializer.parseSchedulePersonHowWhen(obj);

        assertEquals(company, s.getCompany());
        assertEquals(emailNotification, s.getEmailNotification());
        assertEquals(firstName, s.getFirstName());
        assertEquals(forMinute, s.getToTime());
        assertEquals(fromMinute, s.getFromTime());
        assertEquals(id, s.getId());
        assertEquals(lastName, s.getLastName());
        assertEquals(position, s.getPosition());
        assertEquals(pushToMobileApp, s.isPushToMobileApp());
        assertEquals(smsNotification, s.getSmsNotification());
        assertTrue(s.getWeekDays()[0]);
        assertFalse(s.getWeekDays()[1]);
        assertFalse(s.getWeekDays()[2]);
        assertTrue(s.getWeekDays()[3]);
        assertFalse(s.getWeekDays()[4]);
        assertFalse(s.getWeekDays()[5]);
        assertFalse(s.getWeekDays()[6]);
    }
    @Test
    public void testNotificationSchedule() {
        final String description = "JUnit schedule";
        final Long id = 77l;
        final String name = "Sched";

        NotificationSchedule s = new NotificationSchedule();

        s.setDescription(description);
        s.setId(id);
        s.setName(name);
        s.getSchedules().add(createSchedulePersonHowWhen());
        s.getSchedules().add(createSchedulePersonHowWhen());

        final JsonObject obj = serializer.toJson(s).getAsJsonObject();
        s = serializer.parseNotificationSchedule(obj);

        assertEquals(description, s.getDescription());
        assertEquals(id, s.getId());
        assertEquals(name, s.getName());
        assertEquals(2, s.getSchedules().size());
    }
    @Test
    public void testLocationProfile() {
        final String company = "Sun Microsystems";
        final Long id = 77l;
        final boolean interim = true;
        final String name = "JUnit-Location";
        final String notes = "Any notes";
        final String address = "Odessa, Deribasovskaya 1, apt.1";
        final int radius = 1000;
        final boolean start = true;
        final boolean stop = true;
        final double x = 100.500;
        final double y = 100.501;

        LocationProfile p = new LocationProfile();

        p.setCompanyDescription(company);
        p.setId(id);
        p.setInterim(interim);
        p.setName(name);
        p.setNotes(notes);
        p.setAddress(address);
        p.setRadius(radius);
        p.setStart(start);
        p.setStop(stop);
        p.getLocation().setLatitude(x);
        p.getLocation().setLongitude(y);

        final JsonObject obj = serializer.toJson(p).getAsJsonObject();
        p = serializer.parseLocationProfile(obj);

        assertEquals(company, p.getCompanyDescription());
        assertEquals(id, p.getId());
        assertEquals(interim, p.isInterim());
        assertEquals(name, p.getName());
        assertEquals(notes, p.getNotes());
        assertEquals(address, p.getAddress());
        assertEquals(radius, p.getRadius());
        assertEquals(start, p.isStart());
        assertEquals(stop, p.isStop());
        assertEquals(x, p.getLocation().getLatitude(), 0.00001);
        assertEquals(y, p.getLocation().getLongitude(), 0.00001);
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
        final boolean useCurrentTimeForDateShipped = true;
        final boolean useLocationNearestToDevice = true;
        final String assetType = "Trailer";

        ShipmentTemplate t = new ShipmentTemplate();
        t.setAddDateShipped(addDateShipped);
        t.setAlertProfile(alertProfile);
        t.getAlertsNotificationSchedules().add(alertsNotificationSchedule);
        t.setAlertSuppressionDuringCoolDown(alertSuppressionDuringCoolDown);
        t.setArrivalNotificationWithIn(arrivalNotification);
        t.getArrivalNotificationSchedules().add(arrivalNotificationSchedule);
        t.setExcludeNotificationsIfNoAlertsFired(excludeNotificationsIfNoAlertsFired);
        t.setId(id);
        t.setName(name);
        t.setShipmentDescription(shipmentDescription);
        t.setShippedFrom(shippedFrom);
        t.setShippedTo(shippedTo);
        t.setShutdownDeviceTimeOut(shutdownDeviceTimeOut);
        t.setUseCurrentTimeForDateShipped(useCurrentTimeForDateShipped);
        t.setDetectLocationForShippedFrom(useLocationNearestToDevice);
        t.setAssetType(assetType);

        final JsonObject obj = serializer.toJson(t).getAsJsonObject();

        t = serializer.parseShipmentTemplate(obj);

        assertEquals(addDateShipped, t.isAddDateShipped());
        assertNotNull(t.getAlertProfile());
        assertNotNull(t.getAlertsNotificationSchedules());
        assertEquals(alertSuppressionDuringCoolDown, t.getAlertSuppressionDuringCoolDown());
        assertEquals(arrivalNotification, t.getArrivalNotificationWithIn());
        assertNotNull(t.getArrivalNotificationSchedules());
        assertEquals(excludeNotificationsIfNoAlertsFired, t.isExcludeNotificationsIfNoAlertsFired());
        assertEquals(id, t.getId());
        assertEquals(name, t.getName());
        assertEquals(shipmentDescription, t.getShipmentDescription());
        assertNotNull(t.getShippedFrom());
        assertNotNull(t.getShippedTo());
        assertEquals(shutdownDeviceTimeOut, t.getShutdownDeviceTimeOut());
        assertEquals(useCurrentTimeForDateShipped, t.isUseCurrentTimeForDateShipped());
        assertEquals(useLocationNearestToDevice, t.isDetectLocationForShippedFrom());
        assertEquals(assetType, t.getAssetType());
    }
    @Test
    public void testDevice() {
        final String description = "Device description";
        final String imei = "018923475076";
        final String id = "018923475076.123";
        final String name = "Device Name";
        final String sn = "938479";

        Device t = new Device();
        t.setDescription(description);
        t.setId(id);
        t.setImei(imei);
        t.setName(name);
        t.setSn(sn);

        final JsonObject json = serializer.toJson(t).getAsJsonObject();
        t= serializer.parseDevice(json);

        assertEquals(description, t.getDescription());
        assertEquals(id, t.getId());
        assertEquals(imei, t.getImei());
        assertEquals(name, t.getName());
        assertEquals(sn, t.getSn());
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
        final String name = "JUnit-tpl";
        final String shipmentDescription = "Any Description";
        final LocationProfile shippedFrom = createLocationProfile();
        final LocationProfile shippedTo = createLocationProfile();
        final int shutdownDeviceTimeOut = 155;
        final Device t1 = createDevice("234908720394857");
        final Device t2 = createDevice("329847983724987");
        final String palletId = "palettid";
        final Date shipmentDate = new Date(System.currentTimeMillis() - 1000000000l);
        final String customFields = "customFields";
        final ShipmentStatus status = ShipmentStatus.Complete;
        final String assetType = "Trailer";
        final String assetNum = "10515";

        Shipment s = new Shipment();
        s.setAlertProfile(alertProfile);
        s.getAlertsNotificationSchedules().add(alertsNotificationSchedule);
        s.setAlertSuppressionDuringCoolDown(alertSuppressionDuringCoolDown);
        s.setArrivalNotificationWithIn(arrivalNotification);
        s.getArrivalNotificationSchedules().add(arrivalNotificationSchedule);
        s.setExcludeNotificationsIfNoAlertsFired(excludeNotificationsIfNoAlertsFired);
        s.setId(id);
        s.setName(name);
        s.setShipmentDescription(shipmentDescription);
        s.setShippedFrom(shippedFrom);
        s.setShippedTo(shippedTo);
        s.setShutdownDeviceTimeOut(shutdownDeviceTimeOut);
        s.getDevices().add(t1);
        s.getDevices().add(t2);
        s.setPalletId(palletId);
        s.setShipmentDate(shipmentDate);
        s.setCustomFields(customFields);
        s.setStatus(status);
        s.setAssetType(assetType);
        s.setAssetNum(assetNum);

        final JsonObject obj = serializer.toJson(s).getAsJsonObject();
        s = serializer.parseShipment(obj);

        assertNotNull(s.getAlertProfile());
        assertNotNull(s.getAlertsNotificationSchedules());
        assertEquals(alertSuppressionDuringCoolDown, s.getAlertSuppressionDuringCoolDown());
        assertEquals(arrivalNotification, s.getArrivalNotificationWithIn());
        assertNotNull(s.getArrivalNotificationSchedules());
        assertEquals(excludeNotificationsIfNoAlertsFired, s.isExcludeNotificationsIfNoAlertsFired());
        assertEquals(id, s.getId());
        assertEquals(name, s.getName());
        assertEquals(shipmentDescription, s.getShipmentDescription());
        assertNotNull(s.getShippedFrom());
        assertNotNull(s.getShippedTo());
        assertEquals(shutdownDeviceTimeOut, s.getShutdownDeviceTimeOut());
        assertEquals(2, s.getDevices().size());
        assertEquals(palletId, s.getPalletId());
        assertEquals(shipmentDate, s.getShipmentDate());
        assertEquals(customFields, s.getCustomFields());
        assertEquals(status, s.getStatus());
        assertEquals(assetType, s.getAssetType());
        assertEquals(assetNum, s.getAssetNum());
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
    public void testEnterDarkEnvironmentAlertNotification() {
        final Date alertDate = new Date(System.currentTimeMillis() - 100000000l);
        final String alertDescription = "Alert description";
        final Long alertId = 77l;
        final String alertName = "Any name";
        final Device device = createDevice("20394870987324");
        final Shipment shipment = createShipment();
        final AlertType alertType = AlertType.EnterDarkEnvironment;

        Alert alert = new Alert();
        alert.setDate(alertDate);
        alert.setDescription(alertDescription);
        alert.setId(alertId);
        alert.setName(alertName);
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

        assertEquals(alertDate, alert.getDate());
        assertEquals(alertDescription, alert.getDescription());
        assertEquals(alertId, alert.getId());
        assertEquals(alertName, alert.getName());
        assertNotNull(alert.getDevice());
        assertNotNull(alert.getShipment());
        assertEquals(alertType, alert.getType());
    }

    @Test
    public void testArrivalNotification() {
        final Date alertDate = new Date(System.currentTimeMillis() - 100000000l);
        final String alertDescription = "Alert description";
        final Long alertId = 77l;
        final String alertName = "Any name";
        final Device device = createDevice("20394870987324");
        final Shipment shipment = createShipment();
        final AlertType alertType = AlertType.HighTemperature;
        final double temperature = 10.12;
        final int minutes = 75;

        TemperatureAlert alert = new TemperatureAlert();
        alert.setDate(alertDate);
        alert.setDescription(alertDescription);
        alert.setId(alertId);
        alert.setName(alertName);
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

        assertEquals(alertDate, alert.getDate());
        assertEquals(alertDescription, alert.getDescription());
        assertEquals(alertId, alert.getId());
        assertEquals(alertName, alert.getName());
        assertNotNull(alert.getDevice());
        assertNotNull(alert.getShipment());
        assertEquals(alertType, alert.getType());
        assertEquals(temperature, alert.getTemperature(), 0.00001);
        assertEquals(minutes, alert.getMinutes());
    }
    @Test
    public void testTrackerEvent() {
        final int battery = 12;
        final Long id = 7l;
        final double temperature = 77.77;
        final Date time = new Date(System.currentTimeMillis() - 1000000000L);
        final String type = "RSP";
        final double latitude = 10.10;
        final double longitude = 11.11;

        TrackerEvent e = new TrackerEvent();
        e.setBattery(battery);
        e.setId(id);
        e.setTemperature(temperature);
        e.setTime(time);
        e.setType(type);
        e.setLatitude(latitude);
        e.setLongitude(longitude);

        final JsonObject obj= serializer.toJson(e);
        e = serializer.parseTrackerEvent(obj);

        assertEquals(battery, e.getBattery());
        assertEquals(id, e.getId());
        assertEquals(temperature, e.getTemperature(), 0.00001);
        assertEquals(time, e.getTime());
        assertEquals(type, e.getType());
        assertEquals(latitude, e.getLatitude(), 0.000001);
        assertEquals(longitude, e.getLongitude(), 0.00001);
    }
    @Test
    public void testDeviceData() {
        DeviceData d = new DeviceData();

        final Device device = createDevice("93218709879");
        final Shipment shipment = createShipment();

        d.setDevice(device);
        d.getAlerts().add(createAlert(device, shipment, AlertType.BatteryLow));
        d.getAlerts().add(createAlert(device, shipment, AlertType.EnterDarkEnvironment));
        d.getEvents().add(createEvent("AUT"));
        d.getEvents().add(createEvent("DRK"));

        final JsonObject obj = serializer.toJson(d);
        d = serializer.parseDeviceData(obj);

        assertEquals(2, d.getAlerts().size());
        assertEquals(2, d.getEvents().size());
        assertNotNull(d.getDevice());
    }
    @Test
    public void testShipmentData() {
        final Shipment shipment = createShipment();
        final DeviceData d1 = new DeviceData();
        d1.setDevice(createDevice("109823981237049"));
        final DeviceData d2 = new DeviceData();
        d2.setDevice(createDevice("2309875948987987"));

        ShipmentData s = new ShipmentData();
        s.setShipment(shipment);
        s.getDeviceData().add(d1);
        s.getDeviceData().add(d2);

        final JsonObject obj = serializer.toJson(s);
        s = serializer.parseShipmentData(obj);

        assertNotNull(s.getShipment());
        assertEquals(2, s.getDeviceData().size());
    }
    @Test
    public void testUser() {
        final String login = "login";
        final String fullName = "Full Name";

        User u = new User();
        u.setLogin(login);
        u.setFullName(fullName);
        u.getRoles().add(Role.Dispatcher);
        u.getRoles().add(Role.ReportViewer);

        final JsonObject obj = serializer.toJson(u);
        u = serializer.parseUser(obj);

        assertEquals(login, u.getLogin());
        assertEquals(fullName, u.getFullName());
        assertEquals(2, u.getRoles().size());
    }
    @Test
    public void testDeviceCommand() {
        final Device device = createDevice("2380947093287");
        final String command = "shutdown";

        DeviceCommand cmd = new DeviceCommand();
        cmd.setDevice(device);
        cmd.setCommand(command);

        final JsonObject obj = serializer.toJson(cmd).getAsJsonObject();
        cmd = serializer.parseDeviceCommand(obj);

        assertEquals(command, cmd.getCommand());
        assertNotNull(cmd.getDevice());
    }
    public void testUserProfile() {
        UserProfile p = new UserProfile();
        p.getShipments().add(createShipment());
        p.getShipments().add(createShipment());

        final JsonElement obj = serializer.toJson(p);
        p = serializer.parseUserProfile(obj);

        assertEquals(2, p.getShipments().size());
    }
    @Test
    public void testDeviceDcsNativeEvent() {
        DeviceDcsNativeEvent e = new DeviceDcsNativeEvent();

        final int battery = 14;
        final Date date = new Date(System.currentTimeMillis() - 100000l);
        final String imei = "923847924387";
        final double lat = 100.500;
        final double lon = 100.501;
        final String type = "Tracker";

        e.setBattery(battery);
        e.setDate(date);
        e.setImei(imei);
        e.getLocation().setLatitude(lat);
        e.getLocation().setLongitude(lon);
        e.setType(type);

        final JsonElement json = serializer.toJson(e);
        e = serializer.parseDeviceDcsNativeEvent(json);

        assertEquals(battery, e.getBattery());
        assertEquals(date, e.getTime());
        assertEquals(imei, e.getImei());
        assertEquals(lat, e.getLocation().getLatitude(), 0.00001);
        assertEquals(lon, e.getLocation().getLongitude(), 0.00001);
        assertEquals(type, e.getType());
    }
    @Test
    public void testCreateUserRequest() {
        final Company c = new Company();
        c.setId(7l);
        c.setName("JUnit");
        c.setDescription("Test company");
        resolver.add(c);

        final String login = "newuser";
        final String fullName = "Full User Name";
        final String password = "anypassword";

        CreateUserRequest r = new CreateUserRequest();
        final User user = new User();
        user.setLogin(login);
        user.setFullName(fullName);

        r.setCompany(c);
        r.setUser(user);
        r.setPassword(password);

        final JsonElement json = serializer.toJson(r);
        r = serializer.parseCreateUserRequest(json);

        assertNotNull(user);
        assertNotNull(r.getCompany());
        assertEquals(password, r.getPassword());
    }
    @Test
    public void testAlert() {
        final Device device = createDevice("92348072043987");
        final Shipment shipment = createShipment();
        final Date date = new Date(System.currentTimeMillis() - 100000000l);
        final String description = "Alert description";
        final Long id = generateId();
        final String name = "Any name";
        final AlertType type = AlertType.BatteryLow;

        Alert alert = new Alert();
        alert.setDate(date);
        alert.setDescription(description);
        alert.setId(id);
        alert.setName(name);
        alert.setDevice(device);
        alert.setType(type);
        alert.setShipment(shipment);

        final JsonElement json = serializer.toJson(alert);
        alert = serializer.parseAlert(json);

        assertEquals(date, alert.getDate());
        assertEquals(description, alert.getDescription());
        assertEquals(device.getId(), alert.getDevice().getId());
        assertEquals(id, alert.getId());
        assertEquals(name, alert.getName());
        assertEquals(shipment.getId(), alert.getShipment().getId());
        assertEquals(type, alert.getType());
    }
    @Test
    public void testTemparatureAlert() {
        final Device device = createDevice("92348072043987");
        final Shipment shipment = createShipment();
        final Date date = new Date(System.currentTimeMillis() - 100000000l);
        final String description = "Alert description";
        final Long id = generateId();
        final String name = "Any name";
        final AlertType type = AlertType.CriticalHighTemperature;
        final double temperature = -20.3;
        final int minutes = 30;

        TemperatureAlert alert = new TemperatureAlert();
        alert.setDate(date);
        alert.setDescription(description);
        alert.setId(id);
        alert.setName(name);
        alert.setDevice(device);
        alert.setType(type);
        alert.setShipment(shipment);
        alert.setTemperature(temperature);
        alert.setMinutes(minutes);

        final JsonElement json = serializer.toJson(alert);
        alert = (TemperatureAlert) serializer.parseAlert(json);

        assertEquals(date, alert.getDate());
        assertEquals(description, alert.getDescription());
        assertEquals(device.getId(), alert.getDevice().getId());
        assertEquals(id, alert.getId());
        assertEquals(name, alert.getName());
        assertEquals(shipment.getId(), alert.getShipment().getId());
        assertEquals(type, alert.getType());
        assertEquals(temperature, alert.getTemperature(), 0.00001);
        assertEquals(minutes, alert.getMinutes());
    }
    @Test
    public void testArrival() {
        final Device device = createDevice("92348072043987");
        final Shipment shipment = createShipment();
        final Date date = new Date(System.currentTimeMillis() - 100000000l);
        final Long id = 77L;
        final int numberOfMetersOfArrival = 37;

        Arrival a = new Arrival();
        a.setDate(date);
        a.setDevice(device);
        a.setId(id);
        a.setNumberOfMettersOfArrival(numberOfMetersOfArrival);
        a.setShipment(shipment);

        final JsonElement e = serializer.toJson(a);
        a = serializer.parseArrival(e);

        assertEquals(date, a.getDate());
        assertEquals(device.getId(), a.getDevice().getId());
        assertEquals(id, a.getId());
        assertEquals(numberOfMetersOfArrival, a.getNumberOfMettersOfArrival());
        assertEquals(shipment.getId(), a.getShipment().getId());
    }
    @Test
    public void testCompany() {
        final String description = "Company Description";
        final Long id = 77l;
        final String name = "CompanyName";

        Company c = new Company();
        c.setDescription(description);
        c.setId(id);
        c.setName(name);

        final JsonElement json = serializer.toJson(c);
        c = serializer.parseCompany(json);

        assertEquals(description, c.getDescription());
        assertEquals(id, c.getId());
        assertEquals(name, c.getName());
    }
    /**
     * @return tracker event.
     */
    private TrackerEvent createEvent(final String type) {
        final TrackerEvent e = new TrackerEvent();
        e.setId(generateId());
        e.setBattery(1234);
        e.setTemperature(56);
        e.setTime(new Date());
        e.setType(type);
        return e;
    }
    /**
     * @param shipment TODO
     * @return alert.
     */
    private Alert createAlert(final Device device, final Shipment shipment, final AlertType type) {
        final Alert alert = new Alert();
        alert.setDate(new Date(System.currentTimeMillis() - 100000000l));
        alert.setDescription("Alert description");
        alert.setId(generateId());
        alert.setName("Any name");
        alert.setDevice(device);
        alert.setShipment(shipment);
        alert.setType(type);
        return alert;
    }
    /**
     * @param imei IMEI.
     * @return
     */
    private Device createDevice(final String imei) {
        final Device t = new Device();
        t.setDescription("Device description");
        t.setImei(imei);
        t.setId(imei);
        t.setName("Device Name");
        t.setSn("1");
        resolver.add(t);
        return t;
    }
    /**
     * @return
     */
    private NotificationSchedule createNotificationSchedule() {
        final NotificationSchedule s = new NotificationSchedule();
        s.setDescription("JUnit schedule");
        s.setId(generateId());
        s.setName("Sched");
        s.getSchedules().add(createSchedulePersonHowWhen());
        s.getSchedules().add(createSchedulePersonHowWhen());
        resolver.add(s);
        return s;
    }
    /**
     * @return any alert profile.
     */
    private AlertProfile createAlertProfile() {
        final AlertProfile p = new AlertProfile();
        p.setCriticalHighTemperature(5);
        p.setCriticalLowTemperature(-15);
        p.setDescription("Any description");
        p.setHighTemperature(1);
        p.setHighTemperatureForMoreThen(55);
        p.setId(generateId());
        p.setLowTemperature(-10);
        p.setLowTemperatureForMoreThen(55);
        p.setName("AnyAlert");
        p.setWatchBatteryLow(true);
        p.setWatchEnterBrightEnvironment(true);
        p.setWatchEnterDarkEnvironment(true);
        p.setWatchShock(true);
        resolver.add(p);
        return p;
    }
    /**
     * @return any location profile.
     */
    private LocationProfile createLocationProfile() {
        final LocationProfile p = new LocationProfile();

        p.setCompanyDescription("Sun Microsystems");
        p.setId(generateId());
        p.setInterim(true);
        p.setName("JUnit-Location");
        p.setNotes("Any notes");
        p.setAddress("Odessa, Deribasovskaya 1, apt.1");
        p.setRadius(1000);
        p.setStart(true);
        p.setStop(true);
        p.getLocation().setLatitude(100.500);
        p.getLocation().setLongitude(100.501);
        resolver.add(p);
        return p;
    }
    private Shipment createShipment() {
        final Shipment s = new Shipment();
        s.setAlertProfile(createAlertProfile());
        s.getAlertsNotificationSchedules().add(createNotificationSchedule());
        s.setAlertSuppressionDuringCoolDown(55);
        s.setArrivalNotificationWithIn(111);
        s.getArrivalNotificationSchedules().add(createNotificationSchedule());
        s.setExcludeNotificationsIfNoAlertsFired(true);
        s.setId(generateId());
        s.setName("JUnit-tpl");
        s.setShipmentDescription("Any Description");
        s.setShippedFrom(createLocationProfile());
        s.setShippedTo(createLocationProfile());
        s.setShutdownDeviceTimeOut(155);
        s.getDevices().add(createDevice("234908720394857"));
        s.getDevices().add(createDevice("329847983724987"));
        s.setPalletId("palettid");
        s.setShipmentDate(new Date(System.currentTimeMillis() - 1000000000l));
        s.setCustomFields("customFields");
        resolver.add(s);
        return s;
    }
    /**
     * @return
     */
    private Long generateId() {
        return 1000000L + (++lastLong);
    }
    /**
     * @return any schedule/person/how/when
     */
    private PersonalSchedule createSchedulePersonHowWhen() {
        final PersonalSchedule s = new PersonalSchedule();

        s.setCompany("Sun");
        s.setEmailNotification("asuvorov@sun.com");
        s.setFirstName("Alexander");
        s.setToTime(17);
        s.setFromTime(1);
        s.setId(generateId());
        s.setLastName("Suvorov");
        s.setPosition("Generalisimus");
        s.setPushToMobileApp(true);
        s.setSmsNotification("1111111117");
        s.getWeekDays()[0] = true;
        s.getWeekDays()[3] = true;

        return s;
    }
    @SuppressWarnings("unused")
    private void printJSon(final JsonElement e) {
        final String str = gson.toJson(e);
        System.out.println(str);
    }
}
