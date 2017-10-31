/**
 *
 */
package com.visfresh.l12n;

import static com.visfresh.utils.DateTimeUtils.createDateFormat;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.TimeZone;

import com.visfresh.entities.Device;
import com.visfresh.entities.Language;
import com.visfresh.entities.TemperatureUnits;
import com.visfresh.io.shipment.AlertBean;
import com.visfresh.io.shipment.AlertRuleBean;
import com.visfresh.io.shipment.ArrivalBean;
import com.visfresh.io.shipment.NotificationIssueBean;
import com.visfresh.io.shipment.SingleShipmentBean;
import com.visfresh.io.shipment.SingleShipmentLocationBean;
import com.visfresh.io.shipment.TemperatureAlertBean;
import com.visfresh.io.shipment.TemperatureRuleBean;
import com.visfresh.lists.ListShipmentItem;
import com.visfresh.utils.DateTimeUtils;
import com.visfresh.utils.LocalizationUtils;
import com.visfresh.utils.StringUtils;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class NotificationIssueBeanBundle {
    private static final String BUNDLE_NAME = "chart";

    protected final Language lang;
    protected final TimeZone tz;
    protected final TemperatureUnits tu;

    /**
     * @param lang
     * @param tz
     * @param tu
     */
    public NotificationIssueBeanBundle(final Language lang, final TimeZone tz, final TemperatureUnits tu) {
        super();
        this.lang = lang;
        this.tu = tu;
        this.tz = tz;
    }

    /**
     * @param issue alert.
     * @param e tracker event.
     * @return map of replacements.
     */
    protected Map<String, String> createReplacementMap(
            final NotificationIssueBean issue,
            final SingleShipmentLocationBean e,
            final SingleShipmentBean shipment) {

        final Map<String, String> map = new HashMap<String, String>();

        //supported place holders:
        //${date} alert issue date include day and year
        final Date issueDate = issue == null ? e.getTime() : issue.getDate();
        map.put("date", DateTimeUtils.createPrettyFormat(lang, tz).format(issueDate));
        //${time} the time in scope of day.
        final DateFormat sdf = createDateFormat("H:mm", lang, tz);
        map.put("time", sdf.format(issueDate));
        //${device} device IMEI
        map.put("device", shipment.getDevice());
        //${devicesn} device serial number
        map.put("devicesn", normalizeSn(Device.getSerialNumber(shipment.getDevice())));

        //${tripCount} trip count for given device of shipment.
        map.put("tripCount", Integer.toString(shipment.getTripCount()));
        //${shippedFrom}      location shipped from
        map.put("shippedFrom", shipment.getStartLocation() == null
                ? "" : shipment.getStartLocation().getName());
        //${shippedTo}        location shipped to
        map.put("shippedTo", shipment.getEndLocation() == null
                ? "" : shipment.getEndLocation().getName());
        //${shipmentDescription}  the shipment desc
        map.put("shipmentDescription", shipment.getShipmentDescription() == null
                ? "" : shipment.getShipmentDescription());
        map.put("shipmentId", Long.toString(shipment.getShipmentId()));

        if (e != null) {
            //${readingTime}      the time reading occured in user's timezone - eg. 4:34
            map.put("readingTime", createDateFormat("H:mm", lang, tz).format(e.getTime()));
            //${readingDate}      the date reading occured in user's timezone - eg. 12 Feb 2016
            map.put("readingDate", createDateFormat("dd MMM yyyy", lang, tz).format(e.getTime()));
            //${temperature}
            //${readingTemperature}  the temperature in user's temperature scale (C/F) at time of alert
            final String t = LocalizationUtils.getTemperatureString(
                    e.getTemperature(), tu);
            map.put("readingTemperature", t);
            map.put("temperature", t);
        } else {
            map.put("readingTime", "?");
            map.put("readingDate", "?");
            map.put("readingTemperature", "?");
            map.put("temperature", "?");
        }

        if (issue instanceof AlertBean) {

            //for temperature alerts:
            //${type} alert type
            map.put("type", ((AlertBean) issue).getType().toString());

            if (issue instanceof TemperatureAlertBean) {
                final TemperatureAlertBean ta = (TemperatureAlertBean) issue;
                final String period = Integer.toString(ta.getMinutes());
                map.put("period", period);

                //find rule
                final TemperatureRuleBean rule = getRule(ta, shipment.getAlertFired());
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
        } else if (issue instanceof ArrivalBean) {
            final ArrivalBean a = (ArrivalBean) issue;
            map.put("mettersForArrival", Integer.toString(a.getMettersForArrival()));
        }

        return map;
    }
    /**
     * @param issue
     * @param e
     * @param shipment
     * @return
     */
    private Map<String, String> createReplacementMap(final NotificationIssueBean issue, final SingleShipmentLocationBean e,
            final ListShipmentItem shipment) {

        final Map<String, String> map = new HashMap<String, String>();

        //supported place holders:
        //${date} alert issue date include day and year
        final Date issueDate = issue == null ? e.getTime() : issue.getDate();
        map.put("date", DateTimeUtils.createPrettyFormat(lang, tz).format(issueDate));
        //${time} the time in scope of day.
        final DateFormat sdf = createDateFormat("H:mm", lang, tz);
        map.put("time", sdf.format(issueDate));
        //${device} device IMEI
        map.put("device", shipment.getDevice());
        //${devicesn} device serial number
        map.put("devicesn", shipment.getDeviceSN());

        //${tripCount} trip count for given device of shipment.
        map.put("tripCount", Integer.toString(shipment.getTripCount()));
        //${shippedFrom}      location shipped from
        map.put("shippedFrom", shipment.getShippedFrom());
        //${shippedTo}        location shipped to
        map.put("shippedTo", shipment.getShippedTo());
        //${shipmentDescription}  the shipment desc
        map.put("shipmentDescription", shipment.getShipmentDescription() == null
                ? "" : shipment.getShipmentDescription());
        map.put("shipmentId", Long.toString(shipment.getShipmentId()));

        if (e != null) {
            //${readingTime}      the time reading occured in user's timezone - eg. 4:34
            map.put("readingTime", createDateFormat("H:mm", lang, tz).format(e.getTime()));
            //${readingDate}      the date reading occured in user's timezone - eg. 12 Feb 2016
            map.put("readingDate", createDateFormat("dd MMM yyyy", lang, tz).format(e.getTime()));
            //${temperature}
            //${readingTemperature}  the temperature in user's temperature scale (C/F) at time of alert
            final String t = LocalizationUtils.getTemperatureString(
                    e.getTemperature(), tu);
            map.put("readingTemperature", t);
            map.put("temperature", t);
        } else {
            map.put("readingTime", "?");
            map.put("readingDate", "?");
            map.put("readingTemperature", "?");
            map.put("temperature", "?");
        }

        if (issue instanceof AlertBean) {

            //for temperature alerts:
            //${type} alert type
            map.put("type", ((AlertBean) issue).getType().toString());

            if (issue instanceof TemperatureAlertBean) {
                final TemperatureAlertBean ta = (TemperatureAlertBean) issue;
                final String period = Integer.toString(ta.getMinutes());
                map.put("period", period);

                //find rule
                final TemperatureRuleBean rule = getRule(ta, shipment.getTemperatureRules());
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
        } else if (issue instanceof ArrivalBean) {
            final ArrivalBean a = (ArrivalBean) issue;
            map.put("mettersForArrival", Integer.toString(a.getMettersForArrival()));
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
    private <R extends AlertRuleBean> TemperatureRuleBean getRule(
            final TemperatureAlertBean ta, final List<R> rules) {
        if (ta.getRuleId() == null) {
            return null;
        }

        for (final AlertRuleBean rule : rules) {
            if (ta.getRuleId().equals(rule.getId())) {
                final TemperatureRuleBean tr = (TemperatureRuleBean) rule;
                return tr;
            }
        }
        return null;
    }
    /**
     * @param issue notification issue alert/arrival
     * @return bundle key.
     */
    protected String createBundleKey(final NotificationIssueBean issue) {
        String key = "";
        if (issue instanceof AlertBean) {
            final AlertBean alert = (AlertBean) issue;
            key = alert.getType().toString();
            if (alert instanceof TemperatureAlertBean && ((TemperatureAlertBean) alert).isCumulative()) {
                key += ".cumulative";
            }
        } else if (issue instanceof ArrivalBean) {
            return "Arrival";
        }
        return key;
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
     * @param e tracker event.
     * @return description for given alert.
     */
    public String buildDescription(final NotificationIssueBean issue,
            final SingleShipmentLocationBean e, final SingleShipmentBean shipment) {
        final ResourceBundle bundle = ResourceBundle.getBundle(BUNDLE_NAME, XmlControl.INSTANCE);
        final String str = bundle.getString(createBundleKey(issue));
        return StringUtils.getMessage(str, createReplacementMap(issue, e, shipment));
    }
    /**
     * @param alert
     * @param e
     * @param s
     * @return
     */
    public String buildDescription(final NotificationIssueBean issue, final SingleShipmentLocationBean e, final ListShipmentItem s) {
        final ResourceBundle bundle = ResourceBundle.getBundle(BUNDLE_NAME, XmlControl.INSTANCE);
        final String str = bundle.getString(createBundleKey(issue));
        return StringUtils.getMessage(str, createReplacementMap(issue, e, s));
    }
    /**
     * @param user user.
     * @param event event.
     * @return
     */
    public String buildTrackerEventDescription(final SingleShipmentLocationBean event,
            final SingleShipmentBean shipment) {
        final ResourceBundle bundle = ResourceBundle.getBundle(BUNDLE_NAME, XmlControl.INSTANCE);
        final String str = bundle.getString("TrackerEvent");
        return StringUtils.getMessage(str, createReplacementMap(null, event, shipment));
    }
}
