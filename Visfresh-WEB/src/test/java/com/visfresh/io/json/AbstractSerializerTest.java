/**
 *
 */
package com.visfresh.io.json;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.visfresh.controllers.MockReferenceResolver;
import com.visfresh.entities.AlertProfile;
import com.visfresh.entities.AlertRule;
import com.visfresh.entities.AlertType;
import com.visfresh.entities.Device;
import com.visfresh.entities.LocationProfile;
import com.visfresh.entities.NotificationSchedule;
import com.visfresh.entities.PersonSchedule;
import com.visfresh.entities.Shipment;
import com.visfresh.entities.User;
import com.visfresh.utils.SerializerUtils;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class AbstractSerializerTest {
    protected static TimeZone UTC = SerializerUtils.UTС;
    protected MockReferenceResolver resolver = new MockReferenceResolver();
    private long lastLong;

    /**
     * Default constructor.
     */
    public AbstractSerializerTest() {
        super();
    }
    protected Shipment createShipment() {
        final Shipment s = new Shipment();
        s.setAlertProfile(createAlertProfile());
        s.getAlertsNotificationSchedules().add(createNotificationSchedule());
        s.setAlertSuppressionMinutes(55);
        s.setArrivalNotificationWithinKm(111);
        s.getArrivalNotificationSchedules().add(createNotificationSchedule());
        s.setExcludeNotificationsIfNoAlerts(true);
        s.setId(generateId());
        s.setShipmentDescription("Any Description");
        s.setShippedFrom(createLocationProfile());
        s.setShippedTo(createLocationProfile());
        s.setShutdownDeviceTimeOut(155);
        s.setDevice(createDevice("234908720394857"));
        s.setPalletId("palettid");
        s.setShipmentDate(new Date(System.currentTimeMillis() - 1000000000l));
        s.getCustomFields().put("name", "customField1");
        resolver.add(s);
        return s;
    }
    /**
     * @return
     */
    protected Long generateId() {
        return 1000000L + (++lastLong);
    }
    /**
     * @return any schedule/person/how/when
     */
    protected PersonSchedule createPersonSchedule() {
        final PersonSchedule s = new PersonSchedule();

        s.setToTime(17);
        s.setFromTime(1);
        s.setId(generateId());
        s.setSendApp(true);
        s.setUser(createUser("asuvorov"));
        s.getWeekDays()[0] = true;
        s.getWeekDays()[3] = true;

        return s;
    }
    /**
     * @param string
     * @return
     */
    protected User createUser(final String string) {
        final User user = new User();
        user.setEmail("asuvorov@sun.com");
        user.setFirstName("Alexander");
        user.setLastName("Suvorov");
        user.setPosition("Generalisimus");
        user.setPhone("1111111117");
        resolver.add(user);
        return user;
    }
    /**
     * @return any location profile.
     */
    protected LocationProfile createLocationProfile() {
        final LocationProfile p = new LocationProfile();

        p.setCompanyName("Sun Microsystems");
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
    /**
     * @return
     */
    protected NotificationSchedule createNotificationSchedule() {
        final NotificationSchedule s = new NotificationSchedule();
        s.setDescription("JUnit schedule");
        s.setId(generateId());
        s.setName("Sched");
        s.getSchedules().add(createPersonSchedule());
        s.getSchedules().add(createPersonSchedule());
        resolver.add(s);
        return s;
    }
    /**
     * @return any alert profile.
     */
    protected AlertProfile createAlertProfile() {
        final AlertProfile p = new AlertProfile();
        p.setDescription("Any description");
        p.setId(generateId());
        p.setName("AnyAlert");
        p.setWatchBatteryLow(true);
        p.setWatchEnterBrightEnvironment(true);
        p.setWatchEnterDarkEnvironment(true);
        p.setWatchMovementStart(true);
        p.setWatchMovementStop(true);

        AlertRule issue = new AlertRule();
        issue.setTemperature(10);
        issue.setType(AlertType.CriticalHot);
        issue.setTimeOutMinutes(17);
        issue.setCumulativeFlag(true);
        p.getAlertRules().add(issue);

        issue = new AlertRule();
        issue.setTemperature(-3);
        issue.setType(AlertType.Cold);
        issue.setTimeOutMinutes(18);
        issue.setCumulativeFlag(true);
        p.getAlertRules().add(issue);

        resolver.add(p);
        return p;
    }
    /**
     * @param imei IMEI.
     * @return
     */
    protected Device createDevice(final String imei) {
        final Device t = new Device();
        t.setDescription("Device description");
        t.setImei(imei);
        t.setName("Device Name");
        resolver.add(t);
        return t;
    }
    protected static void dumpJson(final JsonElement json) {
        final GsonBuilder b = new GsonBuilder();
        b.setPrettyPrinting();
        b.disableHtmlEscaping();
        System.out.println(b.create().toJson(json));
    }
    /**
     * @param date
     * @return
     */
    protected String format(final Date date) {
        return new SimpleDateFormat("yyyy-MM-dd:HH:mm").format(date);
    }
}
