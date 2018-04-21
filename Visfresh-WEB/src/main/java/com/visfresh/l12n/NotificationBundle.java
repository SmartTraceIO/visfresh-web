/**
 *
 */
package com.visfresh.l12n;

import static com.visfresh.utils.DateTimeUtils.createDateFormat;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.TimeZone;

import org.springframework.stereotype.Component;

import com.visfresh.entities.Alert;
import com.visfresh.entities.Arrival;
import com.visfresh.entities.Device;
import com.visfresh.entities.Language;
import com.visfresh.entities.NotificationIssue;
import com.visfresh.entities.NotificationType;
import com.visfresh.entities.Shipment;
import com.visfresh.entities.TemperatureAlert;
import com.visfresh.entities.TemperatureUnits;
import com.visfresh.entities.TrackerEvent;
import com.visfresh.entities.UserNotification;
import com.visfresh.utils.DateTimeUtils;
import com.visfresh.utils.EntityUtils;
import com.visfresh.utils.LocalizationUtils;
import com.visfresh.utils.StringUtils;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class NotificationBundle extends NotificationIssueBundle {
    private static final String BUNDLE_NAME = "notifications";

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
     * @param user target user.
     * @param issue alert
     * @param trackerEvent tracker event.
     * @return description for given alert.
     */
    public String getSmsMessage(final NotificationIssue issue,
            final TrackerEvent trackerEvent, final Language lang, final TimeZone tz, final TemperatureUnits tu) {
        final String str = getBundle().getString("SMS." + createBundleKey(issue));
        return StringUtils.getMessage(str, createReplacementMap(issue, trackerEvent, lang, tz, tu));
    }
    /**
     * @param user user.
     * @param issue notification issue.
     * @param trackerEvent tracker event.
     * @return message.
     */
    public String getAppMessage(final UserNotification n, final Language lang, final TimeZone tz, final TemperatureUnits tu) {
        final String str = getBundle().getString("App." + createBundleKey(n));
        return StringUtils.getMessage(str, createReplacementMap(n, lang, tz, tu));
    }
    /**
     * @param issue notification issue alert/arrival
     * @return bundle key.
     */
    protected String createBundleKey(final UserNotification n) {
        String key = "";
        if (n.getAlertType() != null) {
            key = n.getAlertType().toString();
            if (n.getAlertType().isTemperatureAlert() && n.isAlertCumulative()) {
                key += ".cumulative";
            }
        } else { //arrival
            return "Arrival";
        }
        return key;
    }
    /**
     * @param shipment the shipment
     * @param user user.
     * @param issue notification issue.
     * @param trackerEvent tracker event.
     * @return message.
     */
    public String getLinkToShipment(final UserNotification n) {
        final String str = getBundle().getString("LinkToShipment");
        final Map<String, String> map = new HashMap<String, String>();
        map.put("shipmentId", n.getShipmentId().toString());
        map.put("devicesn", normalizeSn(Device.getSerialNumber(n.getDevice())));
        map.put("tripCount", Integer.toString(n.getShipmentTripCount()));
        return StringUtils.getMessage(str, map);
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
     * @param user target user.
     * @param issue alert
     * @param trackerEvent tracker event
     * @return description for given alert.
     */
    public String getEmailMessage(final Alert issue,
            final TrackerEvent trackerEvent, final Language lang, final TimeZone tz, final TemperatureUnits tu) {
        final String str = getBundle().getString("Email." + createBundleKey(issue));
        return StringUtils.getMessage(str, createReplacementMap(issue, trackerEvent, lang, tz, tu));
    }
    public String getEmailMessage(final Arrival issue,
            final TrackerEvent trackerEvent, final List<TemperatureAlert> alertsFired,
            final Language lang, final TimeZone tz, final TemperatureUnits tu) {
        final String str = getBundle().getString("Email.Arrival");

        final Map<String, String> map = createReplacementMap(issue, trackerEvent, lang, tz, tu);
        map.put("alerts", new RuleBundle().getAlertsFiredString(
                new LinkedList<>(EntityUtils.getTemperatureRules(alertsFired)), tu));

        return StringUtils.getMessage(str, map);
    }
    public String getArrivalReportEmailMessage(
            final Shipment s, final List<TemperatureAlert> alertsFired,
            final Language lang, final TimeZone tz, final TemperatureUnits tu) {
        final String str = getBundle().getString("Email.ArrivalReport");

        final Map<String, String> map = createReplacementMapForArrivalReport(s, lang, tz, tu, s.getArrivalDate());
        map.put("alerts", new RuleBundle().getAlertsFiredString(
                new LinkedList<>(EntityUtils.getTemperatureRules(alertsFired)), tu));

        return StringUtils.getMessage(str, map);
    }

    /**
     * @param user the user.
     * @param issue notification issue.
     * @param trackerEvent tracker event, can be NULL
     * @return
     */
    public String getEmailSubject(final NotificationIssue issue, final TrackerEvent trackerEvent,
            final Language lang, final TimeZone tz, final TemperatureUnits tu) {
        final String str = getBundle().getString("Email.Subject." + createBundleKey(issue)).trim();
        return StringUtils.getMessage(str, createReplacementMap(issue, trackerEvent, lang, tz, tu));
    }
    public String getArrivalReportEmailSubject(final Shipment s,
            final Language lang, final TimeZone tz, final TemperatureUnits tu) {
        final String str = getBundle().getString("Email.Subject.ArrivalReport").trim();
        return StringUtils.getMessage(str, createReplacementMapForArrivalReport(s, lang, tz, tu, s.getArrivalDate()));
    }
    /**
     * @param issue alert.
     * @param trackerEvent tracker event.
     * @return map of replacements.
     */
    protected Map<String, String> createReplacementMap(final UserNotification n,
            final Language lang, final TimeZone tz, final TemperatureUnits tu) {
        final Date issueDate = n.getIssueDate();

        final Map<String, String> map = new HashMap<String, String>();

        //supported place holders:
        //${date} alert issue date include day and year
        map.put("date", DateTimeUtils.createPrettyFormat(lang, tz).format(issueDate));
        //${time} the time in scope of day.
        final DateFormat sdf = createDateFormat("H:mm", lang, tz);
        map.put("time", sdf.format(issueDate));
        //${device} device IMEI
        map.put("device", n.getDevice());
        //${devicesn} device serial number
        map.put("devicesn", normalizeSn(Device.getSerialNumber(n.getDevice())));

        //${tripCount} trip count for given device of shipment.
        map.put("tripCount", Integer.toString(n.getShipmentTripCount()));
        //${shippedFrom}      location shipped from
        map.put("shippedFrom", n.getShippedFrom() == null
                ? "" : n.getShippedFrom());
        //${shippedTo}        location shipped to
        map.put("shippedTo", n.getShippedTo() == null
                ? "" : n.getShippedTo());
        //${shipmentDescription}  the shipment desc
        map.put("shipmentDescription", n.getShipmentDescription() == null
                ? "" : n.getShipmentDescription());
        map.put("shipmentId", n.getShipmentId().toString());

        if (n.getTrackerEventId() != null) {
            //${readingTime}      the time reading occured in user's timezone - eg. 4:34
            map.put("readingTime", createDateFormat("H:mm", lang, tz).format(n.getEventTime()));
            //${readingDate}      the date reading occured in user's timezone - eg. 12 Feb 2016
            map.put("readingDate", createDateFormat("dd MMM yyyy", lang, tz).format(n.getEventTime()));
            //${temperature}
            //${readingTemperature}  the temperature in user's temperature scale (C/F) at time of alert
            final String t = LocalizationUtils.getTemperatureString(
                    n.getTemperature(), tu);
            map.put("readingTemperature", t);
            map.put("temperature", t);
        } else {
            map.put("readingTime", "?");
            map.put("readingDate", "?");
            map.put("readingTemperature", "?");
            map.put("temperature", "?");
        }

        if (n.getAlertType() != null) {

            //for temperature alerts:
            //${type} alert type
            map.put("type", n.getAlertType().name());

            if (n.getAlertType().isTemperatureAlert()) {
                final String period = Integer.toString(n.getAlertMinutes());
                map.put("period", period);

                //find rule
                if (n.getAlertRuleTimeOutMinutes() == null) {//old version
                    //${ruleperiod}       the time period in alert rule
                    map.put("ruleperiod", period);
                } else {//new version which supports the rule ID.
                    map.put("ruleperiod", Integer.toString(n.getAlertRuleTimeOutMinutes()));
                }
                if (n.getAlertRuleTemperature() == null) {//old version
                    //${ruletemperature}  the temperature in alert rule
                    map.put("ruletemperature", LocalizationUtils.getTemperatureString(
                            n.getTemperature(), tu));
                } else {//new version which supports the rule ID.
                    map.put("ruletemperature", LocalizationUtils.getTemperatureString(
                            n.getAlertRuleTemperature(), tu));
                }
            }
        } else if (n.getType() == NotificationType.Arrival) {
            map.put("mettersForArrival", Integer.toString(n.getNumberOfMettersOfArrival()));
        }

        return map;
    }
    /**
     * @param issue alert.
     * @param trackerEvent tracker event.
     * @return map of replacements.
     */
    private Map<String, String> createReplacementMapForArrivalReport(
            final Shipment shipment, final Language lang, final TimeZone tz, final TemperatureUnits tu,
            final Date issueDate) {
        final Device device = shipment.getDevice();

        final Map<String, String> map = new HashMap<String, String>();

        //supported place holders:
        //${date} alert issue date include day and year
        map.put("date", DateTimeUtils.createPrettyFormat(lang, tz).format(issueDate));
        //${time} the time in scope of day.
        final DateFormat sdf = createDateFormat("H:mm", lang, tz);
        map.put("time", sdf.format(issueDate));
        //${device} device IMEI
        map.put("device", device.getImei());
        //${devicesn} device serial number
        map.put("devicesn", normalizeSn(device.getSn()));

        if (shipment != null) {
            //${tripCount} trip count for given device of shipment.
            map.put("tripCount", Integer.toString(shipment.getTripCount()));
            //${shippedFrom}      location shipped from
            map.put("shippedFrom", shipment.getShippedFrom() == null
                    ? "" : shipment.getShippedFrom().getName());
            //${shippedTo}        location shipped to
            map.put("shippedTo", shipment.getShippedTo() == null
                    ? "" : shipment.getShippedTo().getName());
            //${shipmentDescription}  the shipment desc
            map.put("shipmentDescription", shipment.getShipmentDescription() == null
                    ? "" : shipment.getShipmentDescription());
            map.put("shipmentId", shipment.getId().toString());
        }

        map.put("readingTime", "?");
        map.put("readingDate", "?");
        map.put("readingTemperature", "?");
        map.put("temperature", "?");

        return map;
    }
    /**
     * @return resource bundle.
     */
    private ResourceBundle getBundle() {
        return ResourceBundle.getBundle(BUNDLE_NAME, XmlControl.INSTANCE);
    }
}
