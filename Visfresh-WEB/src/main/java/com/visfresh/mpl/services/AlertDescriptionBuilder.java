/**
 *
 */
package com.visfresh.mpl.services;

import static com.visfresh.utils.DateTimeUtils.createDateFormat;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import org.springframework.stereotype.Component;

import com.visfresh.entities.Alert;
import com.visfresh.entities.AlertRule;
import com.visfresh.entities.Arrival;
import com.visfresh.entities.ShipmentIssue;
import com.visfresh.entities.TemperatureAlert;
import com.visfresh.entities.TemperatureUnits;
import com.visfresh.entities.TrackerEvent;
import com.visfresh.entities.User;
import com.visfresh.io.json.AbstractJsonSerializer;
import com.visfresh.l12n.XmlControl;
import com.visfresh.utils.StringUtils;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class AlertDescriptionBuilder {
    /**
     * Default constructor.
     */
    public AlertDescriptionBuilder() {
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
    public String buildDescription(final ShipmentIssue issue, final User user) {
        final ResourceBundle bundle = ResourceBundle.getBundle("alerts", XmlControl.INSTANCE);
        final String str = bundle.getString(createBundleKey(issue));
        return StringUtils.getMessage(str, createReplacementMap(issue, user));
    }

    /**
     * @param alert
     * @return
     */
    private String createBundleKey(final ShipmentIssue issue) {
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
    /**
     * @param issue alert.
     * @return map of replacements.
     */
    private Map<String, String> createReplacementMap(final ShipmentIssue issue, final User user) {
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
                map.put("temperature", getTemperatureString(ta.getTemperature(), user.getTemperatureUnits()));
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
     * @param t temperature.
     * @param units temperature units.
     * @return temperature string.
     */
    private String getTemperatureString(final double t, final TemperatureUnits units) {
        final double temp = getTemperature(t, units);
        String degree;
        switch (units) {
            case Fahrenheit:
                degree = "\u00B0F";
                break;
                default:
                    degree = "\u00B0C";
                    //nothing
                    break;
        }

        //create US locale decimal format
        final DecimalFormat fmt = new DecimalFormat("#0.0");
        final DecimalFormatSymbols decimalFormatSymbols = new DecimalFormatSymbols(Locale.US);
        fmt.setDecimalFormatSymbols(decimalFormatSymbols);

        //format temperature string
        return fmt.format(temp) + degree;
    }
    /**
     * TODO move to resource bundles.
     * @param rule
     * @return
     */
    public String alertRuleToString(final AlertRule rule, final TemperatureUnits units) {
        final StringBuilder sb = new StringBuilder();
        switch (rule.getType()) {
            case Cold:
            case CriticalCold:
                sb.append('<');
                break;
            case Hot:
            case CriticalHot:
                sb.append('>');
                break;
            case Battery:
                return "battery low";
            case LightOff:
                return "light off";
            case LightOn:
                return "light on";
                default:
                    throw new IllegalArgumentException("Unexpected alert type: " + rule.getType());
        }

        //only temperature alert rules. Other should be returned before.
        sb.append(getTemperatureString(rule.getTemperature(), units));
        //append time
        sb.append(" for " + rule.getTimeOutMinutes() + " min");
        //append total
        if (rule.isCumulativeFlag()) {
            sb.append(" in total");
        }
        return sb.toString();
    }


    /**
     * @param tCelsium
     * @param units
     * @return
     */
    public static double getTemperature(final double tCelsium, final TemperatureUnits units) {
        double temp;
        switch (units) {
            case Fahrenheit:
                temp = tCelsium * 1.8 + 32;
                break;
                default:
                    temp = tCelsium;
                    //nothing
                    break;
        }
        return temp;
    }

    /**
     * @param a
     * @param user
     * @return
     */
    public String buildShortDescription(final Alert a, final User user) {
        final StringBuilder sb = new StringBuilder();
        //temperature: "0.62",
        if (a instanceof TemperatureAlert) {
            final TemperatureAlert ta = (TemperatureAlert) a;
//            line1: "3.22°C at 12:01am on 12 Aug 2014",
            sb.append(getTemperatureString(ta.getTemperature(), user.getTemperatureUnits()));
        } else {
            switch (a.getType()) {
                case Battery:
                    sb.append("battery low");
                    break;
                case LightOff:
                    sb.append("light off");
                    break;
                case LightOn:
                    sb.append("light on");
                    break;
                default:
                    throw new IllegalArgumentException("Unexpected alert type: " + a.getType());
            }

        }

        sb.append(" at ");

        final DateFormat fmt = createDateFormat(user, "h:mmaa 'on' d MMM yyyy");
        sb.append(fmt.format(a.getDate()));

        return sb.toString();
    }

    /**
     * TODO move to resource bundles.
     * @param event event.
     * @param user user.
     * @return
     */
    public String buildLastReadingDescription(final TrackerEvent event, final User user) {
        final StringBuilder sb = new StringBuilder();
            sb.append("Last reading for Tracker #");
            sb.append(event.getDevice().getSn());
            sb.append("(");
            sb.append(event.getShipment().getTripCount());
            sb.append(")");
        return sb.toString();
    }

    /**
     * @param event event.
     * @param user user.
     * @return short tracker event description.
     */
    public String buildShortDescription(final TrackerEvent event, final User user) {
        // 4.34°C at 9:20pm on 12 Aug 2014
        final StringBuilder sb = new StringBuilder();
        sb.append(getTemperatureString(event.getTemperature(), user.getTemperatureUnits()));

        sb.append(" at ");

        final DateFormat fmt = createDateFormat(user, "h:mmaa 'on' d MMM yyyy");
        sb.append(fmt.format(event.getTime()));

        return sb.toString();
    }

    /**
     * TODO localize
     * @param a arrival.
     * @param user user.
     * @return arrival description.
     */
    public String buildDescriptionForSingleShipment(final Arrival a, final User user) {
        return "Arrival Notification for Tracker #"
                + a.getDevice().getSn()
                + "("
                + a.getShipment().getTripCount()
                + ")";
    }
    /**
     * TODO localize
     * @param a arrival.
     * @param user user.
     * @return short arrival description.
     */
    public String buildShortDescription(final Arrival a, final User user) {
        //<20kms away at 7:20pm on 12 Aug 2015
        final StringBuilder sb = new StringBuilder();
        sb.append('<');
        sb.append(a.getNumberOfMettersOfArrival());
        sb.append("m away at ");

        final DateFormat fmt = createDateFormat(user, "h:mmaa 'on' d MMM yyyy");
        sb.append(fmt.format(a.getDate()));

        return sb.toString();
    }
}
