/**
 *
 */
package com.visfresh.mpl.services;

import static com.visfresh.utils.DateTimeUtils.createDateFormat;

import java.text.DateFormat;
import java.util.HashMap;
import java.util.Map;

import com.visfresh.entities.Alert;
import com.visfresh.entities.Arrival;
import com.visfresh.entities.NotificationIssue;
import com.visfresh.entities.TemperatureAlert;
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
     * @return map of replacements.
     */
    protected Map<String, String> createReplacementMap(final NotificationIssue issue,
            final User user) {
        final Map<String, String> map = new HashMap<String, String>();
        //supported place holders:
        //${date} alert issue date include day and year
        DateFormat sdf = createDateFormat(user, AbstractJsonSerializer.DATE_FORMAT);
        map.put("date", sdf.format(issue.getDate()));
        //${time} the time in scope of day.
        sdf = createDateFormat(user, "HH:mm");
        map.put("time", sdf.format(issue.getDate()));
        //${device} device IMEI
        map.put("device", issue.getDevice().getImei());
        //${devicesn} device serial number
        map.put("devicesn", issue.getDevice().getSn());
        //${tripCount} trip count for given device of shipment.
        map.put("tripCount", Integer.toString(issue.getShipment().getTripCount()));

        if (issue instanceof Alert) {
            //for temperature alerts:
            //${type} alert type
            map.put("type", ((Alert) issue).getType().toString());

            if (issue instanceof TemperatureAlert) {
                final TemperatureAlert ta = (TemperatureAlert) issue;
                //${temperature}
                map.put("temperature", LocalizationUtils.getTemperatureString(
                        ta.getTemperature(), user.getTemperatureUnits()));
                //${period}
                map.put("period", Integer.toString(ta.getMinutes()));
            }
        } else if (issue instanceof Arrival) {
            final Arrival a = (Arrival) issue;
            map.put("mettersForArrival", Integer.toString(a.getNumberOfMettersOfArrival()));
        }

        return map;
    }

    /**
     * @param alert
     * @return
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
