/**
 *
 */
package com.visfresh.l12n;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Date;

import junit.framework.AssertionFailedError;

import org.junit.Before;
import org.junit.Test;

import com.visfresh.entities.Alert;
import com.visfresh.entities.AlertProfile;
import com.visfresh.entities.AlertType;
import com.visfresh.entities.Arrival;
import com.visfresh.entities.Device;
import com.visfresh.entities.LocationProfile;
import com.visfresh.entities.NotificationIssue;
import com.visfresh.entities.Shipment;
import com.visfresh.entities.ShipmentStatus;
import com.visfresh.entities.TemperatureAlert;
import com.visfresh.entities.TemperatureRule;
import com.visfresh.entities.TemperatureUnits;
import com.visfresh.entities.TrackerEvent;
import com.visfresh.entities.TrackerEventType;
import com.visfresh.entities.User;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class ChartBundleTest extends ChartBundle {
    private Shipment shipment;
    private User user;
    private TrackerEvent trackerEvent;

    /**
     * Default constructor.
     */
    public ChartBundleTest() {
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
        user.setTemperatureUnits(TemperatureUnits.Fahrenheit);

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
    public void testAlertBundle() {
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
                String msg = buildDescription(user, alert, trackerEvent);
                assertNotNull(msg);
                assertPlaceholdersResolved(alert, msg);

                //cumulative
                alert.setCumulative(true);

                msg = buildDescription(user, alert, trackerEvent);
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

                final String msg = buildDescription(user, alert, trackerEvent);
                assertNotNull(msg);
                assertPlaceholdersResolved(alert, msg);
            }
        }
    }
    @Test
    public void testArrivalBundle() {

        //Arrival
        final Arrival arrival = new Arrival();
        arrival.setDate(new Date());
        arrival.setDevice(shipment.getDevice());
        arrival.setShipment(shipment);

        final String msg = buildDescription(user, arrival, trackerEvent);
        assertNotNull(msg);
        assertPlaceholdersResolved(arrival, msg);
    }
    @Test
    public void testTrackerEventBundle() {
        final String msg = buildTrackerEventDescription(user, trackerEvent);
        assertNotNull(msg);
        assertPlaceholdersResolved(null, msg);
    }
    @Test
    public void testRuleVariablesSupport() {
        final int period = 1899875;
        final double temperature = 123.456;

        //Temperature alerts
        final TemperatureAlert alert = new TemperatureAlert();
        alert.setDate(new Date());
        alert.setDevice(shipment.getDevice());
        alert.setShipment(shipment);
        alert.setType(AlertType.Hot);
        alert.setMinutes(period);
        alert.setTemperature(temperature);

        //check without rule set
        String msg = buildDescription(user, alert, trackerEvent);
        assertTrue(msg.indexOf(Integer.toString(period)) > 0);

        //check with rule set
        final TemperatureRule rule = new TemperatureRule(AlertType.Hot);
        this.shipment.setAlertProfile(new AlertProfile());
        this.shipment.getAlertProfile().getAlertRules().add(rule);

        final Long ruleId = 125l;
        final double ruleTemperature = 654.321;
        final int rulePeriod = 987658;

        rule.setId(ruleId);
        rule.setTemperature(ruleTemperature);
        rule.setTimeOutMinutes(rulePeriod);
        alert.setRuleId(ruleId);

        msg = buildDescription(user, alert, trackerEvent);
        assertFalse(msg.indexOf(Integer.toString(period)) > 0);
        assertTrue(msg.indexOf(Integer.toString(rulePeriod)) > 0);
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
