/**
 *
 */
package com.visfresh.l12n;

import static com.visfresh.utils.DateTimeUtils.createDateFormat;
import static com.visfresh.utils.DateTimeUtils.createIsoFormat;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.TimeZone;

import org.springframework.stereotype.Component;

import com.visfresh.entities.Device;
import com.visfresh.entities.Language;
import com.visfresh.entities.NotificationIssue;
import com.visfresh.entities.Shipment;
import com.visfresh.entities.TemperatureUnits;
import com.visfresh.entities.TrackerEvent;
import com.visfresh.mpl.services.NotificationIssueBundle;
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
     * @param trackerEvent tracker event
     * @return description for given alert.
     */
    public String getEmailMessage(final NotificationIssue issue,
            final TrackerEvent trackerEvent, final Language lang, final TimeZone tz, final TemperatureUnits tu) {
        final String str = getBundle().getString("Email." + createBundleKey(issue));
        return StringUtils.getMessage(str, createReplacementMap(issue, trackerEvent, lang, tz, tu));
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
    public String getAppMessage(final NotificationIssue issue,
            final TrackerEvent trackerEvent, final Language lang, final TimeZone tz, final TemperatureUnits tu) {
        final String str = getBundle().getString("App." + createBundleKey(issue));
        return StringUtils.getMessage(str, createReplacementMap(issue, trackerEvent, lang, tz, tu));
    }
    /**
     * @param shipment the shipment
     * @param user user.
     * @param issue notification issue.
     * @param trackerEvent tracker event.
     * @return message.
     */
    public String getLinkToShipment(final Shipment shipment) {
        final String str = getBundle().getString("LinkToShipment");
        final Map<String, String> map = new HashMap<String, String>();
        map.put("shipmentId", shipment.getId().toString());
        map.put("devicesn", normalizeSn(shipment.getDevice().getSn()));
        map.put("tripCount", Integer.toString(shipment.getTripCount()));
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
    public String getArrivalReportEmailMessage(final Shipment s,
            final Language lang, final TimeZone tz, final TemperatureUnits tu) {
        final String str = getBundle().getString("Email.ArrivalReport");
        return StringUtils.getMessage(str, createReplacementMap(s, lang, tz, tu, s.getArrivalDate()));
    }
    public String getArrivalReportEmailSubject(final Shipment s,
            final Language lang, final TimeZone tz, final TemperatureUnits tu) {
        final String str = getBundle().getString("Email.Subject.ArrivalReport").trim();
        return StringUtils.getMessage(str, createReplacementMap(s, lang, tz, tu, s.getArrivalDate()));
    }
    /**
     * @param issue alert.
     * @param trackerEvent tracker event.
     * @return map of replacements.
     */
    private Map<String, String> createReplacementMap(
            final Shipment shipment, final Language lang, final TimeZone tz, final TemperatureUnits tu,
            final Date issueDate) {
        final Device device = shipment.getDevice();

        final Map<String, String> map = new HashMap<String, String>();

        //supported place holders:
        //${date} alert issue date include day and year
        map.put("date", createIsoFormat(lang, tz).format(issueDate));
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
