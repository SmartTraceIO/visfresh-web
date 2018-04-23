/**
 *
 */
package com.visfresh.l12n;

import static org.junit.Assert.assertNotNull;

import java.util.Date;
import java.util.LinkedList;

import org.junit.Before;
import org.junit.Test;

import com.visfresh.entities.Alert;
import com.visfresh.entities.AlertType;
import com.visfresh.entities.AppUserNotification;
import com.visfresh.entities.Arrival;
import com.visfresh.entities.Device;
import com.visfresh.entities.LocationProfile;
import com.visfresh.entities.NotificationIssue;
import com.visfresh.entities.NotificationType;
import com.visfresh.entities.Shipment;
import com.visfresh.entities.ShipmentStatus;
import com.visfresh.entities.TemperatureAlert;
import com.visfresh.entities.TrackerEvent;
import com.visfresh.entities.TrackerEventType;
import com.visfresh.entities.User;

import junit.framework.AssertionFailedError;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class NotificationBundleTest extends NotificationBundle {
    private Shipment shipment;
    private User user;
    private TrackerEvent trackerEvent;

    /**
     * Default constructor.
     */
    public NotificationBundleTest() {
        super();
    }

    @Before
    public void setUp() {
        final Device device = new Device();
        device.setImei("90812730982372");
        device.setName("JUnit device");

        final LocationProfile shippedTo = createLocation("Target location", 11, 11);
        final LocationProfile shippedFrom = createLocation("Start location", 1, 1);

        shipment = new Shipment();
        shipment.setId(7L);
        shipment.setAssetNum("poNum");
        shipment.setArrivalNotificationWithinKm(10);
        shipment.setAssetType("assetType");
        shipment.setDevice(device);
        shipment.setLastEventDate(new Date(System.currentTimeMillis() - 10000000l));
        shipment.setShippedTo(shippedTo);
        shipment.setShippedFrom(shippedFrom);
        shipment.setShipmentDescription("Shipment description");
        shipment.setStatus(ShipmentStatus.InProgress);
        shipment.setShipmentDate(new Date(System.currentTimeMillis() - 100000000l));

        user = new User();
        user.setId(7l);
        user.setActive(true);
        user.setEmail("developer@visfresh.com");
        user.setFirstName("JUnit");
        user.setLastName("JUnit");

        trackerEvent = new TrackerEvent();
        trackerEvent.setId(7l);
        trackerEvent.setBattery(78787);
        trackerEvent.setDevice(shipment.getDevice());
        trackerEvent.setShipment(shipment);
        trackerEvent.setTemperature(36.6);
        trackerEvent.setTime(new Date());
        trackerEvent.setType(TrackerEventType.AUT);
    }
    @Test
    public void testBundles() {
        for (final AlertType type : AlertType.values()) {
            if (type.isTemperatureAlert()) {
                //Temperature alerts
                final TemperatureAlert alert = new TemperatureAlert();
                alert.setDate(new Date());
                alert.setDevice(shipment.getDevice());
                alert.setShipment(shipment);
                alert.setType(type);

                //not cumulative
                alert.setCumulative(false);

                //mail
                String msg = getEmailMessage(alert, trackerEvent, user.getLanguage(),
                        user.getTimeZone(), user.getTemperatureUnits());
                assertNotNull(msg);
                assertPlaceholdersResolved(alert, msg);

                msg = getEmailSubject(alert, trackerEvent, user.getLanguage(),
                        user.getTimeZone(), user.getTemperatureUnits());
                assertNotNull(msg);
                assertPlaceholdersResolved(alert, msg);

                //SMS
                msg = getSmsMessage(alert, trackerEvent, user.getLanguage(),
                        user.getTimeZone(), user.getTemperatureUnits());
                assertNotNull(msg);
                assertPlaceholdersResolved(alert, msg);

                //cumulative
                alert.setCumulative(true);

                //mail
                msg = getEmailMessage(alert, trackerEvent, user.getLanguage(),
                        user.getTimeZone(), user.getTemperatureUnits());
                assertNotNull(msg);
                assertPlaceholdersResolved(alert, msg);

                msg = getEmailSubject(alert, trackerEvent, user.getLanguage(),
                        user.getTimeZone(), user.getTemperatureUnits());
                assertNotNull(msg);
                assertPlaceholdersResolved(alert, msg);

                //SMS
                msg = getSmsMessage(alert, trackerEvent, user.getLanguage(),
                        user.getTimeZone(), user.getTemperatureUnits());
                assertNotNull(msg);
                assertPlaceholdersResolved(alert, msg);
            }
        }

        //Other alerts
        for (final AlertType type : AlertType.values()) {
            if (!type.isTemperatureAlert()) {
                final Alert alert = new Alert();

                alert.setDate(new Date());
                alert.setDevice(shipment.getDevice());
                alert.setShipment(shipment);
                alert.setType(type);

                //mail
                String msg = getEmailMessage(alert, trackerEvent, user.getLanguage(),
                        user.getTimeZone(), user.getTemperatureUnits());
                assertNotNull(msg);
                assertPlaceholdersResolved(alert, msg);

                msg = getEmailSubject(alert, trackerEvent, user.getLanguage(),
                        user.getTimeZone(), user.getTemperatureUnits());
                assertNotNull(msg);
                assertPlaceholdersResolved(alert, msg);

                //SMS
                msg = getSmsMessage(alert, trackerEvent, user.getLanguage(),
                        user.getTimeZone(), user.getTemperatureUnits());
                assertNotNull(msg);
                assertPlaceholdersResolved(alert, msg);
            }
        }

        //Arrival
        final Arrival arrival = new Arrival();
        arrival.setDate(new Date());
        arrival.setDevice(shipment.getDevice());
        arrival.setShipment(shipment);

        //mail
        String msg = getEmailMessage(arrival, trackerEvent, new LinkedList<TemperatureAlert>(),
                user.getLanguage(), user.getTimeZone(), user.getTemperatureUnits());
        assertNotNull(msg);
        assertPlaceholdersResolved(arrival, msg);

        msg = getEmailSubject(arrival, trackerEvent, user.getLanguage(),
                user.getTimeZone(), user.getTemperatureUnits());
        assertNotNull(msg);
        assertPlaceholdersResolved(arrival, msg);

        //SMS
        msg = getSmsMessage(arrival, trackerEvent, user.getLanguage(),
                user.getTimeZone(), user.getTemperatureUnits());
        assertNotNull(msg);
        assertPlaceholdersResolved(arrival, msg);
    }

    @Test
    public void testAppMessageBundles() {
        for (final AlertType type : AlertType.values()) {
            if (type.isTemperatureAlert()) {
                //Temperature alerts
                final AppUserNotification n = createNotification();

                n.setType(NotificationType.Alert);
                n.setAlertType(type);

                //not cumulative
                n.setAlertCumulative(false);
                n.setAlertMinutes(10);
                n.setTemperature(33.);

                //App
                String msg = getAppMessage(n, user.getLanguage(),
                        user.getTimeZone(), user.getTemperatureUnits());
                assertNotNull(msg);
                assertPlaceholdersResolved(n, msg);

                //cumulative
                n.setAlertCumulative(true);

                //App
                msg = getAppMessage(n, user.getLanguage(),
                        user.getTimeZone(), user.getTemperatureUnits());
                assertNotNull(msg);
                assertPlaceholdersResolved(n, msg);
            }
        }

        //Other alerts
        for (final AlertType type : AlertType.values()) {
            if (!type.isTemperatureAlert()) {
                final AppUserNotification n = createNotification();
                n.setType(NotificationType.Alert);

                //App
                final String msg = getAppMessage(n, user.getLanguage(),
                        user.getTimeZone(), user.getTemperatureUnits());
                assertNotNull(msg);
                assertPlaceholdersResolved(n, msg);
            }
        }

        //Arrival
        final AppUserNotification n = createNotification();
        n.setType(NotificationType.Arrival);

        //App
        final String msg = getAppMessage(n, user.getLanguage(),
                user.getTimeZone(), user.getTemperatureUnits());
        assertNotNull(msg);
        assertPlaceholdersResolved(n, msg);
    }

    /**
     * @return
     */
    private AppUserNotification createNotification() {
        final AppUserNotification n = new AppUserNotification();
        n.setIssueDate(new Date());
        n.setDevice(shipment.getDevice().getImei());
        n.setShipmentId(shipment.getId());
        n.setShipmentDescription(shipment.getShipmentDescription());
        n.setShipmentTripCount(shipment.getTripCount());
        return n;
    }

    @Test
    public void testGetLinkToShipment() {
        final AppUserNotification n = createNotification();

        final String link = getLinkToShipment(n);
        assertNotNull(link);
        assertPlaceholdersResolved((AppUserNotification) null, link);
    }
    /**
     * @param issue notification issue.
     * @param msg message.
     */
    private void assertPlaceholdersResolved(final NotificationIssue issue, final String msg) {
        if (msg.contains("{")) {
            throw new AssertionFailedError("Not all placeholders resolved for message '"
                    + msg +"' of " + createBundleKey(issue));
        }
    }
    /**
     * @param n notification issue.
     * @param msg message.
     */
    private void assertPlaceholdersResolved(final AppUserNotification n, final String msg) {
        if (msg.contains("{")) {
            throw new AssertionFailedError("Not all placeholders resolved for message '"
                    + msg +"' of " + createBundleKey(n));
        }
    }

    /**
     * @param name
     * @param latitude
     * @param longitude
     * @return
     */
    private LocationProfile createLocation(final String name, final int latitude, final int longitude) {
        final LocationProfile loc = new LocationProfile();
        loc.setName(name);
        loc.setAddress("Address of " + name);
        loc.getLocation().setLatitude(latitude);
        loc.getLocation().setLongitude(longitude);
        return loc;
    }
}
