/**
 *
 */
package com.visfresh.mpl.services;

import static com.visfresh.utils.DateTimeUtils.createDateFormat;
import static com.visfresh.utils.LocalizationUtils.getTemperatureString;

import java.text.DateFormat;
import java.util.ResourceBundle;

import org.springframework.stereotype.Component;

import com.visfresh.entities.Alert;
import com.visfresh.entities.AlertRule;
import com.visfresh.entities.Arrival;
import com.visfresh.entities.NotificationIssue;
import com.visfresh.entities.TemperatureRule;
import com.visfresh.entities.TemperatureAlert;
import com.visfresh.entities.TemperatureUnits;
import com.visfresh.entities.TrackerEvent;
import com.visfresh.entities.User;
import com.visfresh.l12n.XmlControl;
import com.visfresh.utils.StringUtils;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class AlertBundle extends NotificationIssueBundle {
    /**
     * Default constructor.
     */
    public AlertBundle() {
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
    public String buildDescription(final NotificationIssue issue, final User user) {
        final ResourceBundle bundle = ResourceBundle.getBundle("alerts", XmlControl.INSTANCE);
        final String str = bundle.getString(createBundleKey(issue));
        return StringUtils.getMessage(str, createReplacementMap(issue, user));
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

        if (rule instanceof TemperatureRule) {
            final TemperatureRule tr = (TemperatureRule) rule;
            //only temperature alert rules. Other should be returned before.
            sb.append(getTemperatureString(tr.getTemperature(), units));
            //append time
            sb.append(" for " + tr.getTimeOutMinutes() + " min");
            //append total
            if (tr.isCumulativeFlag()) {
                sb.append(" in total");
            }
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
