/**
 *
 */
package com.visfresh.mpl.services;

import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

import org.springframework.stereotype.Component;

import com.visfresh.entities.Location;
import com.visfresh.entities.LocationProfile;
import com.visfresh.entities.NotificationIssue;
import com.visfresh.entities.Shipment;
import com.visfresh.entities.User;
import com.visfresh.l12n.XmlControl;
import com.visfresh.utils.LocalizationUtils;
import com.visfresh.utils.LocationUtils;
import com.visfresh.utils.StringUtils;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class NotificationBundle extends NotificationIssueBundle {
    /**
     * The bundles.
     */
    private final ResourceBundle bundle = ResourceBundle.getBundle("notifications", XmlControl.INSTANCE);

    /**
     * Default constructor.
     */
    public NotificationBundle() {
        super();
    }

    /**
     *  supported place holders:
     *    ${date} alert issue date include day and year
     *    ${time} the time in scope of day.
     *    ${type} alert type
     *    ${device} device IMEI
     *    ${devicesn} device serial number
     *    ${tripCount} trip count for given device of shipment.
     *
     *  for temperature alerts:
     *    ${temperature}
     *    ${period}
     *
     * @param issue alert
     * @param user target user.
     * @return description for given alert.
     */
    public String getEmailMessage(final NotificationIssue issue, final User user) {
        final String str = bundle.getString("Email." + createBundleKey(issue));
        return StringUtils.getMessage(str, createReplacementMap(issue, user));
    }
    /**
     *  supported place holders:
     *    ${date} alert issue date include day and year
     *    ${time} the time in scope of day.
     *    ${type} alert type
     *    ${device} device IMEI
     *    ${devicesn} device serial number
     *    ${tripCount} trip count for given device of shipment.
     *
     *  for temperature alerts:
     *    ${temperature}
     *    ${period}
     *
     * @param issue alert
     * @param user target user.
     * @return description for given alert.
     */
    public String getSmsMessage(final NotificationIssue issue, final User user) {
        final String str = bundle.getString("SMS." + createBundleKey(issue));
        return StringUtils.getMessage(str, createReplacementMap(issue, user));
    }
    /**
     * @param issue notification issue.
     * @param user user.
     * @return message.
     */
    public String getAppMessage(final NotificationIssue issue, final User user) {
        final String str = bundle.getString("App." + createBundleKey(issue));
        return StringUtils.getMessage(str, createReplacementMap(issue, user));
    }
    /**
     * @param issue notification issue.
     * @param user the user.
     * @return
     */
    public String getEmailSubject(final NotificationIssue issue, final User user) {
        final String str = bundle.getString("Email.Subject." + createBundleKey(issue));
        return StringUtils.getMessage(str, createReplacementMap(issue, user));
    }
    /**
     * @param issue notification issue.
     * @param user user.
     * @return subject for SMS.
     */
    public String getSmsSubject(final NotificationIssue issue, final User user) {
        final String str = bundle.getString("Email.Subject." + createBundleKey(issue));
        return StringUtils.getMessage(str, createReplacementMap(issue, user));
    }
    /**
     * @param issue notification issue.
     * @param user user.
     * @return notification issue subject.
     */
    public String getAppSubject(final NotificationIssue issue, final User user) {
        final String str = bundle.getString("App.Subject." + createBundleKey(issue));
        return StringUtils.getMessage(str, createReplacementMap(issue, user));
    }

    /**
     * @param shipment the shipment.
     * @param user the suer.
     * @return shipment description.
     */
    public String getShipmentDescription(final Shipment shipment, final User user) {
        final String str = bundle.getString("App.Shipment");
        return StringUtils.getMessage(str, createReplacementMap(shipment, user));
    }
    /**
     * @param loc location profile.
     * @param location current location.
     * @param user user.
     * @return location description.
     */
    public String getLocationDescription(final LocationProfile loc,
            final Location location, final User user) {
        final String str = bundle.getString("App.Location");
        return StringUtils.getMessage(str, createReplacementMap(loc, location, user));
    }

    /**
     * @param loc location profile.
     * @param location location.
     * @param user
     * @return
     */
    private Map<String, String> createReplacementMap(final LocationProfile loc,
            final Location location, final User user) {
        final Map<String, String> map = new HashMap<String, String>();

        final double distance;
        if (location != null) {
            distance = LocationUtils.getDistanceMeters(
                    location.getLatitude(),
                    location.getLongitude(),
                    loc.getLocation().getLatitude(),
                    loc.getLocation().getLongitude());

            map.put("latitude", Double.toString(location.getLatitude()));
            map.put("longitude", Double.toString(location.getLongitude()));
        } else {
            distance = 0;

            map.put("latitude", "0");
            map.put("longitude", "0");
        }

        map.put("distance", LocalizationUtils.getDistanceString(
                (int) Math.round(distance), user.getMeasurementUnits()));

        map.put("targetLatitude", Double.toString(loc.getLocation().getLatitude()));
        map.put("targetLongitude", Double.toString(loc.getLocation().getLongitude()));

        map.put("targetName", loc.getName());
        map.put("targetAddress", loc.getAddress());
        return map;
    }

    /**
     * @param shipment shipment user.
     * @param user
     * @return
     */
    private Map<String, String> createReplacementMap(final Shipment shipment,
            final User user) {
        final Map<String, String> map = new HashMap<String, String>();

        map.put("description", shipment.getShipmentDescription());
        //${device} device IMEI
        map.put("device", shipment.getDevice().getImei());
        //${devicesn} device serial number
        map.put("devicesn", shipment.getDevice().getSn());
        //${tripCount} trip count for given device of shipment.
        map.put("tripCount", Integer.toString(shipment.getTripCount()));

        return map;
    }
}
