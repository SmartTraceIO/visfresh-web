/**
 *
 */
package com.visfresh.mpl.services;

import static com.visfresh.utils.DateTimeUtils.createDateFormat;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.visfresh.entities.Alert;
import com.visfresh.entities.Arrival;
import com.visfresh.entities.Device;
import com.visfresh.entities.NotificationIssue;
import com.visfresh.entities.Shipment;
import com.visfresh.entities.TemperatureAlert;
import com.visfresh.entities.TrackerEvent;
import com.visfresh.entities.User;
import com.visfresh.io.json.AbstractJsonSerializer;
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
    protected Map<String, String> createReplacementMap(final User user,
            final NotificationIssue issue, final TrackerEvent trackerEvent) {
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
        DateFormat sdf = createDateFormat(user, AbstractJsonSerializer.DATE_FORMAT);
        map.put("date", sdf.format(issueDate));
        //${time} the time in scope of day.
        sdf = createDateFormat(user, "HH:mm");
        map.put("time", sdf.format(issueDate));
        //${device} device IMEI
        map.put("device", device.getImei());
        //${devicesn} device serial number
        map.put("devicesn", device.getSn());

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
        } else {
            map.put("tripCount", "");
            map.put("shippedFrom", "");
            map.put("shippedTo", "");
            map.put("shipmentDescription", "");
        }

        if (trackerEvent != null) {
            //${readingTime}      the time reading occured in user's timezone - eg. 4:34am
            map.put("readingTime", createDateFormat(user, "K:mma").format(trackerEvent.getTime()));
            //${readingDate}      the date reading occured in user's timezone - eg. 12 Feb 2016
            map.put("readingDate", createDateFormat(user, "dd MMM yyyy").format(trackerEvent.getTime()));
            //${temperature}
            //${readingTemperature}  the temperature in user's temperature scale (C/F) at time of alert
            final String t = LocalizationUtils.getTemperatureString(
                    trackerEvent.getTemperature(), user.getTemperatureUnits());
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
                //${ruletemperature}  the temperature in alert rule
                map.put("ruletemperature", LocalizationUtils.getTemperatureString(
                        ta.getTemperature(), user.getTemperatureUnits()));
                //${ruleperiod}       the time period in alert rule
                final String period = Integer.toString(ta.getMinutes());
                map.put("ruleperiod", period);
                map.put("period", period);
            }
        } else if (issue instanceof Arrival) {
            final Arrival a = (Arrival) issue;
            map.put("mettersForArrival", Integer.toString(a.getNumberOfMettersOfArrival()));
        }

        return map;
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
