/**
 *
 */
package com.visfresh.mpl.services;

import static org.junit.Assert.assertNotNull;

import java.util.Date;

import junit.framework.AssertionFailedError;

import org.junit.Before;
import org.junit.Test;

import com.visfresh.entities.Alert;
import com.visfresh.entities.AlertType;
import com.visfresh.entities.Arrival;
import com.visfresh.entities.Device;
import com.visfresh.entities.Location;
import com.visfresh.entities.LocationProfile;
import com.visfresh.entities.NotificationIssue;
import com.visfresh.entities.Shipment;
import com.visfresh.entities.ShipmentStatus;
import com.visfresh.entities.TemperatureAlert;
import com.visfresh.entities.User;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class NotificationBundleTest extends NotificationBundle {
    private Shipment shipment;
    private User user;

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
                String msg = getEmailMessage(alert, user);
                assertNotNull(msg);
                assertPlaceholdersResolved(alert, msg);

                msg = getEmailSubject(alert, user);
                assertNotNull(msg);
                assertPlaceholdersResolved(alert, msg);

                //SMS
                msg = getSmsMessage(alert, user);
                assertNotNull(msg);
                assertPlaceholdersResolved(alert, msg);

                msg = getEmailSubject(alert, user);
                assertNotNull(msg);
                assertPlaceholdersResolved(alert, msg);

                //App
                msg = getAppMessage(alert, user);
                assertNotNull(msg);
                assertPlaceholdersResolved(alert, msg);

                msg = getEmailSubject(alert, user);
                assertNotNull(msg);
                assertPlaceholdersResolved(alert, msg);

                //cumulative
                alert.setCumulative(true);

                //mail
                msg = getEmailMessage(alert, user);
                assertNotNull(msg);
                assertPlaceholdersResolved(alert, msg);

                msg = getEmailSubject(alert, user);
                assertNotNull(msg);
                assertPlaceholdersResolved(alert, msg);

                //SMS
                msg = getSmsMessage(alert, user);
                assertNotNull(msg);
                assertPlaceholdersResolved(alert, msg);

                msg = getEmailSubject(alert, user);
                assertNotNull(msg);
                assertPlaceholdersResolved(alert, msg);

                //App
                msg = getAppMessage(alert, user);
                assertNotNull(msg);
                assertPlaceholdersResolved(alert, msg);

                msg = getEmailSubject(alert, user);
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
                String msg = getEmailMessage(alert, user);
                assertNotNull(msg);
                assertPlaceholdersResolved(alert, msg);

                msg = getEmailSubject(alert, user);
                assertNotNull(msg);
                assertPlaceholdersResolved(alert, msg);

                //SMS
                msg = getSmsMessage(alert, user);
                assertNotNull(msg);
                assertPlaceholdersResolved(alert, msg);

                msg = getEmailSubject(alert, user);
                assertNotNull(msg);
                assertPlaceholdersResolved(alert, msg);

                //App
                msg = getAppMessage(alert, user);
                assertNotNull(msg);
                assertPlaceholdersResolved(alert, msg);

                msg = getEmailSubject(alert, user);
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
        String msg = getEmailMessage(arrival, user);
        assertNotNull(msg);
        assertPlaceholdersResolved(arrival, msg);

        msg = getEmailSubject(arrival, user);
        assertNotNull(msg);
        assertPlaceholdersResolved(arrival, msg);

        //SMS
        msg = getSmsMessage(arrival, user);
        assertNotNull(msg);
        assertPlaceholdersResolved(arrival, msg);

        msg = getEmailSubject(arrival, user);
        assertNotNull(msg);
        assertPlaceholdersResolved(arrival, msg);

        //App
        msg = getAppMessage(arrival, user);
        assertNotNull(msg);
        assertPlaceholdersResolved(arrival, msg);

        msg = getEmailSubject(arrival, user);
        assertNotNull(msg);
        assertPlaceholdersResolved(arrival, msg);
    }
    @Test
    public void testAppShipmentDescription() {
        final String msg = getShipmentDescription(shipment, user);
        assertNotNull(msg);
        if (msg.contains("${")) {
            throw new AssertionFailedError("Not all placeholders resolved for message '"
                    + msg +"' of shipment description");
        }
    }
    @Test
    public void testAppLocationDescription() {
        final Location location = new Location(22, 22);
        final String msg = getLocationDescription(shipment.getShippedTo(), location, user);
        assertNotNull(msg);
        if (msg.contains("${")) {
            throw new AssertionFailedError("Not all placeholders resolved for message '"
                    + msg +"' of location description");
        }
    }

    /**
     * @param issue notification issue.
     * @param msg message.
     */
    private void assertPlaceholdersResolved(final NotificationIssue issue, final String msg) {
        if (msg.contains("${")) {
            throw new AssertionFailedError("Not all placeholders resolved for message '"
                    + msg +"' of " + createBundleKey(issue));
        }
    }
}
