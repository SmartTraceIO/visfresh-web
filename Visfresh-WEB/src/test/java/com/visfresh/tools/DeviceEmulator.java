/**
 *
 */
package com.visfresh.tools;

import java.awt.geom.Point2D;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TimeZone;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.visfresh.controllers.restclient.AlertProfileRestClient;
import com.visfresh.controllers.restclient.NotificationScheduleRestClient;
import com.visfresh.controllers.restclient.ShipmentRestClient;
import com.visfresh.entities.AlertProfile;
import com.visfresh.entities.AlertType;
import com.visfresh.entities.Device;
import com.visfresh.entities.Location;
import com.visfresh.entities.NotificationSchedule;
import com.visfresh.entities.PersonSchedule;
import com.visfresh.entities.ShipmentStatus;
import com.visfresh.entities.TemperatureRule;
import com.visfresh.entities.TemperatureUnits;
import com.visfresh.entities.User;
import com.visfresh.io.ShipmentDto;
import com.visfresh.lists.DeviceDto;
import com.visfresh.lists.ListAlertProfileItem;
import com.visfresh.lists.ListNotificationScheduleItem;
import com.visfresh.services.RestServiceException;
import com.visfresh.utils.SerializerUtils;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class DeviceEmulator extends AbstractTool implements Runnable {
    /**
     *
     */
    private static final TimeZone UTС = SerializerUtils.UTС;
    private final AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
    private final Random random = new Random();
    private String imei;
    private int battery = 10;
    private int temperature = 7;
    private Location startLocation = new Location(82, 50); //Sidney
    private Location location = new Location(startLocation.getLatitude(), startLocation.getLongitude());
    private Point2D.Double v = new Point2D.Double(0.005, 0.);
    private long startTime;

    private AlertProfileRestClient alertProfileClient = new AlertProfileRestClient(UTС, TemperatureUnits.Celsius);
    private NotificationScheduleRestClient notificationScheduleClient = new NotificationScheduleRestClient(UTС);
    private ShipmentRestClient service;

    /**
     * @param url
     * @param userName
     * @param password
     * @throws IOException
     * @throws RestServiceException
     */
    public DeviceEmulator(final String url)
            throws IOException, RestServiceException {
        super(url);
        context.scan(SpringConfig.class.getPackage().getName());
        context.refresh();
    }
    /* (non-Javadoc)
     * @see com.visfresh.tools.AbstractTool#initalize(java.lang.String, java.lang.String)
     */
    @Override
    protected void initalize(final String userName, final String password)
            throws IOException, RestServiceException {
        super.initalize(userName, password);

        service = new ShipmentRestClient(user);
        service.setAuthToken(userService.getAuthToken());
    }

    public AlertProfile createAlertProfileIfNeed() throws RestServiceException, IOException {
        final String name = "DevTools Alerts";

        final List<ListAlertProfileItem> profiles = this.alertProfileClient.getAlertProfiles(1, 1000);
        for (final ListAlertProfileItem alertProfile : profiles) {
            if (name.equals(alertProfile.getAlertProfileName())) {
                return alertProfileClient.getAlertProfile(alertProfile.getAlertProfileId());
            }
        }

        final AlertProfile profile = new AlertProfile();

        profile.setCompany(company);
        profile.setName(name);

        //hot
        TemperatureRule issue = new TemperatureRule(AlertType.Hot);
        issue.setTemperature(15.);
        issue.setTimeOutMinutes(5);
        issue.setCumulativeFlag(true);
        profile.getAlertRules().add(issue);

        //critical hot
        issue = new TemperatureRule(AlertType.CriticalHot);
        issue.setTemperature(20.);
        issue.setTimeOutMinutes(1);
        profile.getAlertRules().add(issue);

        profile.setDescription("Development tool profile");

        //log
        issue = new TemperatureRule(AlertType.Cold);
        issue.setTemperature(3.);
        issue.setTimeOutMinutes(10);
        issue.setCumulativeFlag(true);
        profile.getAlertRules().add(issue);

        //critical cold
        issue = new TemperatureRule(AlertType.CriticalCold);
        issue.setTemperature(-5.);
        issue.setTimeOutMinutes(1);
        profile.getAlertRules().add(issue);

        profile.setWatchBatteryLow(true);
        profile.setWatchEnterBrightEnvironment(true);
        profile.setWatchEnterDarkEnvironment(true);
        profile.setWatchMovementStart(true);
        profile.setWatchMovementStop(true);

        final Long id = alertProfileClient.saveAlertProfile(profile);
        profile.setId(id);

        return profile;
    }

    /* (non-Javadoc)
     * @see java.lang.Runnable#run()
     */
    @Override
    public void run() {
        final int maxPause = 20000;
        final int minPause = 10000;
        startTime = System.currentTimeMillis();

        try {
            while (true) {
                sendSystemDeviceEvent();
                Thread.sleep(minPause + random.nextInt(maxPause - minPause));
            }
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     *
     */
    private void sendSystemDeviceEvent() {
        final JsonObject obj = createEvent();
        sendEvent(obj);
    }

    /**
     * @param obj
     */
    private void sendEvent(final JsonObject obj) {
        final String TABLE = "systemmessages";
        final String TYPE_FIELD = "type";
        final String TYME_FIELD = "time";
        final String RETRYON_FIELD = "retryon";
        final String MESSAGE_FIELD = "message";

        //insert
        final String sql = "insert into " + TABLE + " (" +
                TYME_FIELD
                + "," + TYPE_FIELD
                + "," + RETRYON_FIELD
                + "," + MESSAGE_FIELD
             + ")" + " values("
                + ":"+ TYME_FIELD
                + ", :" + TYPE_FIELD
                + ", :" + RETRYON_FIELD
                + ", :" + MESSAGE_FIELD
                + ")";

        final Map<String, Object> paramMap = new HashMap<String, Object>();
        paramMap.put(TYME_FIELD, new Date());
        paramMap.put(TYPE_FIELD, "Tracker");
        paramMap.put(RETRYON_FIELD, new Date());
        paramMap.put(MESSAGE_FIELD, obj.toString());

        final NamedParameterJdbcTemplate jdbc = context.getBean(NamedParameterJdbcTemplate.class);
        jdbc.update(sql, paramMap);
    }

    /**
     * @return
     */
    protected JsonObject createEvent() {
        final JsonObject obj = new JsonObject();
        //update battery
        battery += (random.nextBoolean()? 1 : - 1);
        battery = Math.max(0, battery);
        battery = Math.min(battery, 10);

        //update temperature
        temperature += (random.nextBoolean()? 1 : - 1);
        temperature = Math.max(-10, temperature);
        temperature = Math.min(temperature, 16);

        //update location
        final double dt = (System.currentTimeMillis() - startTime) / 60000L;
        final double lat = location.getLatitude() * v.y * dt;
        final double lon = location.getLongitude();
        location.setLatitude(lat);
        location.setLongitude(lon);

        obj.addProperty("battery", battery);
        obj.addProperty("temperature", temperature);
        obj.addProperty("time", new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ").format(new Date()));
        obj.addProperty("type", "AUT");
        obj.addProperty("latitude", location.getLatitude());
        obj.addProperty("longitude", location.getLongitude());
        obj.addProperty("imei", imei);
        return obj;
    }

    /**
     * @throws RestServiceException
     * @throws IOException
     */
    protected void initializeShipment() throws RestServiceException, IOException {
        final Device device = createDeviceIfNeed();
        createShipmentIfNeed(device);
        this.imei = device.getImei();
    }

    /**
     * @param device
     * @return
     * @throws IOException
     * @throws RestServiceException
     */
    private ShipmentDto createShipmentIfNeed(final Device device) throws RestServiceException, IOException {
        final String description = "DevelopmentShipment";

        final JsonArray shipments = service.getShipments(1, 100000);
        for (final JsonElement e : shipments) {
            final JsonObject obj = e.getAsJsonObject();
            final Long id = obj.get("shipmentId").getAsLong();
            final ShipmentDto shipment = service.getShipment(id);
            if (description.equals(shipment.getShipmentDescription())
                    && shipment.getStatus() != ShipmentStatus.Ended) {
                return shipment;
            }
        }

        //create shipment with default alert
        final ShipmentDto s = new ShipmentDto();

        s.setDeviceImei(device.getImei());
        s.setShipmentDescription(description);
        s.setAlertProfile(createAlertProfileIfNeed().getId());
        s.getAlertsNotificationSchedules().add(createNotificationScheduleIfNeed().getId());

        final Long id = service.saveShipment(s, null, false).getShipmentId();
        s.setId(id);

        return s;
    }

    /**
     * @return
     * @throws IOException
     * @throws RestServiceException
     */
    private NotificationSchedule createNotificationScheduleIfNeed() throws RestServiceException, IOException {
        final String name = "Test Schedule";

        final List<ListNotificationScheduleItem> schedules = notificationScheduleClient.getNotificationSchedules(null, null);
        for (final ListNotificationScheduleItem s : schedules) {
            if (name.equals(s.getNotificationScheduleName())) {
                return notificationScheduleClient.getNotificationSchedule(s.getId());
            }
        }

        final NotificationSchedule s = new NotificationSchedule();
        s.setCompany(company);
        s.setDescription("Test Notification schedule");
        s.setName(name);

        s.getSchedules().add(createSchedule("Vyacheslav", "Soldatov", "vyacheslav.soldatov@inbox.ru", null, "Java Developer"));
        s.getSchedules().add(createSchedule("James", "Richardson", "james@smarttrace.com.au", "+61414910052", "Manager"));

        final Long id = notificationScheduleClient.saveNotificationSchedule(s);
        s.setId(id);
        return s;
    }

    /**
     * @param firstName
     * @param lastName
     * @param email
     * @param phone
     * @param position company position
     * @return
     * @throws RestServiceException
     * @throws IOException
     */
    private PersonSchedule createSchedule(final String firstName, final String lastName,
            final String email, final String phone, final String position)
                    throws IOException, RestServiceException {
        final User user = new User();
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEmail(email);
        user.setPhone(phone);
        user.setPosition(position);
        user.setCompany(company);
        userService.saveUser(user, "", false);

        final PersonSchedule s = new PersonSchedule();
        s.setFromTime(1);
        s.setToTime(23 * 60 + 55);
        s.setSendApp(true);
        s.setUser(user);
        Arrays.fill(s.getWeekDays(), true);
        return s;
    }

    /**
     * @throws IOException
     * @throws RestServiceException
     *
     */
    private Device createDeviceIfNeed() throws RestServiceException, IOException {
        final String id = "111111";

        final List<DeviceDto> devices = deviceService.getDevices(null, true, 1, 100000);
        for (final DeviceDto d : devices) {
            if (id.equals(d.getImei())) {
                final Device device = new Device();
                device.setCompany(company);
                device.setDescription(d.getDescription());
                device.setImei(d.getImei());
                device.setModel(d.getModel());
                device.setName(d.getName());
                return device;
            }
        }

        final Device d = new Device();
        d.setCompany(company);
        d.setDescription("Develment Tools virtual device");
        d.setImei("111111111111111");
        d.setName("DevTool Device");

        deviceService.saveDevice(d);
        return d;
    }

    public static void main(final String[] args) throws Exception {
        final DeviceEmulator emulator = new DeviceEmulator("http://localhost:8080/web/vf");
        emulator.initalize("globaladmin", args[0]);

        try {
            emulator.initializeShipment();
            emulator.run();
        } finally {
            emulator.context.destroy();
        }
    }
}
