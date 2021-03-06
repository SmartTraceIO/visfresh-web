/**
 *
 */
package com.visfresh.l12n;

import static com.visfresh.utils.DateTimeUtils.createDateFormat;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

import com.visfresh.entities.Alert;
import com.visfresh.entities.Arrival;
import com.visfresh.entities.Device;
import com.visfresh.entities.Language;
import com.visfresh.entities.NotificationIssue;
import com.visfresh.entities.Shipment;
import com.visfresh.entities.TemperatureAlert;
import com.visfresh.entities.TemperatureRule;
import com.visfresh.entities.TemperatureUnits;
import com.visfresh.entities.TrackerEvent;
import com.visfresh.utils.DateTimeUtils;
import com.visfresh.utils.LocalizationUtils;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class NotificationIssueBundle {

    /**
     *
     */
    public NotificationIssueBundle() {
        super();
    }

    /**
     * @param issue alert.
     * @param trackerEvent tracker event.
     * @return map of replacements.
     */
    protected Map<String, String> createReplacementMap(
            final NotificationIssue issue, final TrackerEvent trackerEvent,
            final Language lang, final TimeZone tz, final TemperatureUnits tu) {
        final Shipment shipment;
        final Date issueDate;
        final Device device;

        if (issue != null) {
            shipment = issue.getShipment();
            issueDate = issue.getDate();
            device = issue.getDevice();
        } else {
            shipment = trackerEvent.getShipment();
            device = trackerEvent.getDevice();
            issueDate = trackerEvent.getTime();
        }

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
        } else {
            map.put("shipmentId", "");
            map.put("tripCount", "");
            map.put("shippedFrom", "");
            map.put("shippedTo", "");
            map.put("shipmentDescription", "");
        }

        if (trackerEvent != null) {
            //${readingTime}      the time reading occured in user's timezone - eg. 4:34
            map.put("readingTime", createDateFormat("H:mm", lang, tz).format(trackerEvent.getTime()));
            //${readingDate}      the date reading occured in user's timezone - eg. 12 Feb 2016
            map.put("readingDate", createDateFormat("dd MMM yyyy", lang, tz).format(trackerEvent.getTime()));
            //${temperature}
            //${readingTemperature}  the temperature in user's temperature scale (C/F) at time of alert
            final String t = LocalizationUtils.getTemperatureString(
                    trackerEvent.getTemperature(), tu);
            map.put("readingTemperature", t);
            map.put("temperature", t);
        } else {
            map.put("readingTime", "?");
            map.put("readingDate", "?");
            map.put("readingTemperature", "?");
            map.put("temperature", "?");
        }

        if (issue instanceof Alert) {

            //for temperature alerts:
            //${type} alert type
            map.put("type", ((Alert) issue).getType().toString());

            if (issue instanceof TemperatureAlert) {
                final TemperatureAlert ta = (TemperatureAlert) issue;
                final String period = Integer.toString(ta.getMinutes());
                map.put("period", period);

                //find rule
                final TemperatureRule rule = getRule(ta);
                if (rule == null) {//old version
                    //${ruleperiod}       the time period in alert rule
                    map.put("ruleperiod", period);
                    //${ruletemperature}  the temperature in alert rule
                    map.put("ruletemperature", LocalizationUtils.getTemperatureString(
                            ta.getTemperature(), tu));
                } else {//new version which supports the rule ID.
                    map.put("ruleperiod", Integer.toString(rule.getTimeOutMinutes()));
                    //${ruletemperature}  the temperature in alert rule
                    map.put("ruletemperature", LocalizationUtils.getTemperatureString(
                            rule.getTemperature(), tu));
                }
            }
        } else if (issue instanceof Arrival) {
            final Arrival a = (Arrival) issue;
            map.put("mettersForArrival", Integer.toString(a.getNumberOfMettersOfArrival()));
        }

        return map;
    }
    /**
     * @param sn serial number.
     * @return normalized serial number.
     */
    protected String normalizeSn(final String sn) {
        final StringBuilder sb = new StringBuilder(sn);
        while (sb.charAt(0) == '0' && sb.length() > 1) {
            sb.deleteCharAt(0);
        }
        return sb.toString();
    }
    /**
     * @param ta temperature alert.
     * @return temperature rule.
     */
    private TemperatureRule getRule(final TemperatureAlert ta) {
        if (ta.getRuleId() != null && ta.getShipment().getAlertProfile() != null) {
            for (final TemperatureRule rule: ta.getShipment().getAlertProfile().getAlertRules()) {
                if (ta.getRuleId().equals(rule.getId())) {
                    return rule;
                }
            }
        }
        return null;
    }
    /**
     * @param issue notification issue alert/arrival
     * @return bundle key.
     */
    protected String createBundleKey(final NotificationIssue issue) {
        String key = "";
        if (issue instanceof Alert) {
            final Alert alert = (Alert) issue;
            key = alert.getType().toString();
            if (alert instanceof TemperatureAlert && ((TemperatureAlert) alert).isCumulative()) {
                key += ".cumulative";
            }
        } else if (issue instanceof Arrival) {
            return "Arrival";
        }
        return key;
    }
}
