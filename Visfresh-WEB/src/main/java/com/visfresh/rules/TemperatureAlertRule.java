/**
 *
 */
package com.visfresh.rules;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.visfresh.dao.TrackerEventDao;
import com.visfresh.entities.Alert;
import com.visfresh.entities.Language;
import com.visfresh.entities.TemperatureAlert;
import com.visfresh.entities.TemperatureRule;
import com.visfresh.entities.TrackerEvent;
import com.visfresh.rules.state.ShipmentSession;
import com.visfresh.utils.DateTimeUtils;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class TemperatureAlertRule extends AbstractAlertRule {
    /**
     *
     */
    private static final long MINUTE = 60000l;

    /**
     * Rule name.
     */
    public static final String NAME = "TemperatureAlert";

    @Autowired
    private TrackerEventDao trackerEventDao;

    /**
     * Default constructor.
     */
    public TemperatureAlertRule() {
        super();
    }

    /* (non-Javadoc)
     * @see com.visfresh.drools.AbstractAlertRule#getName()
     */
    @Override
    public final String getName() {
        return NAME;
    }

    /* (non-Javadoc)
     * @see com.visfresh.rules.AbstractAlertRule#handleInternal(com.visfresh.entities.TrackerEvent)
     */
    @Override
    protected Alert[] handleInternal(final RuleContext context) {
        final TrackerEvent event = context.getEvent();
        final TrackerEvent prev = trackerEventDao.getPreviousEvent(event);
        final double t = getAverageTemparature(prev, event);
        final ShipmentSession session = context.getSessionManager().getSession(event.getShipment());

        final List<Alert> alerts = new LinkedList<Alert>();

        //process alert rules
        for (final TemperatureRule rule : event.getShipment().getAlertProfile().getAlertRules()) {

            //if rule is not already processed. Each rule should be processed one time.
            if (!AbstractRuleEngine.isTemperatureRuleProcessed(session, rule)
                    || canProcessAgain(session, rule)) {
                if (isMatches(rule, t)) {
                    Alert a = null;
                    if (rule.isCumulativeFlag()) {
                        //process cumulative rule
                        if (prev != null) {
                            a = processComulativeRule(rule, context, prev);
                        }
                    } else {
                        //process normal rule
                        a = processNormalRule(rule, context);
                    }

                    //add rule to rule list
                    if (a != null) {
                        alerts.add(a);
                    }
                } else {
                    clearNotCummulativeDates(session, rule);
                }
            }
        }

        return alerts.toArray(new Alert[alerts.size()]);
    }

    /**
     * @param rule rule.
     * @param context rule context.
     * @param prev previous event.
     */
    private Alert processComulativeRule(final TemperatureRule rule, final RuleContext context,
            final TrackerEvent prev) {
        final ShipmentSession session = context.getSessionManager().getSession(context.getEvent().getShipment());
        final Map<String, String> props = session.getTemperatureAlerts()
                .getProperties();

        //process cumulative rule
        final String cumulativeTotalKey = createCumulativeTotalKey(rule);
        final TrackerEvent event = context.getEvent();

        //update summary
        final String totalStr = props.get(cumulativeTotalKey);
        long total = totalStr == null ? 0 : Long.parseLong(totalStr);
        total += Math.abs(event.getTime().getTime() - prev.getTime().getTime());

        if (shouldFireAlert(rule, session, event.getTime(), total)) {
            return fireAlert(rule, session, event, total);
        } else {
            props.put(cumulativeTotalKey, Long.toString(total));
        }

        return null;
    }
    /**
     * @param rule rule.
     * @param context rule context.
     * @return alert.
     */
    private Alert processNormalRule(final TemperatureRule rule, final RuleContext context) {
        final ShipmentSession session = context.getSessionManager().getSession(context.getEvent().getShipment());
        final Map<String, Date> dateProps = session.getTemperatureAlerts()
                .getDates();

        final String startIssueKey = createStartIssueKey(rule);
        final Date firstIssue = dateProps.get(startIssueKey);
        if (firstIssue == null) {
            //if first issue, return immediately
            dateProps.put(startIssueKey, context.getEvent().getTime());
            return null;
        }

        final TrackerEvent event = context.getEvent();
        final long total = (event.getTime().getTime() - firstIssue.getTime());
        if (shouldFireAlert(rule, session, event.getTime(), total)) {
            return fireAlert(rule, session, event, total);
        }

        return null;
    }

    /**
     * @param rule
     * @param session
     * @param event
     * @param total
     * @return
     */
    protected TemperatureAlert fireAlert(final TemperatureRule rule, final ShipmentSession session,
            final TrackerEvent event, final long total) {
        final TemperatureAlert a = createAlert(rule, event);
        a.setMinutes((int) (total / MINUTE));

        //mark the rule already processed
        AbstractRuleEngine.setProcessedTemperatureRule(session, rule);

        //set last processed date to shipment property
        session.setShipmentProperty(createAlertTimeKey(rule),
                createTimeFormat().format(event.getTime()));

        //clear processing fields
        clearNotCummulativeDates(session, rule);
        clearCummulativeDates(session, rule);
        return a;
    }

    /**
     * @param rule
     * @param total
     * @return
     */
    protected boolean shouldFireAlert(final TemperatureRule rule,
            final ShipmentSession session, final Date date, final long total) {
        if (rule.getMaxRateMinutes() != null && AbstractRuleEngine.isTemperatureRuleProcessed(session, rule)) {
            final Date lastAlert = getLastAlertTime(session, rule);
            if (lastAlert == null || date.getTime() - lastAlert.getTime() < rule.getMaxRateMinutes() * MINUTE) {
                return false;
            }
        }

        return total >= rule.getTimeOutMinutes() * MINUTE;
    }
    /**
     * @param session
     * @param rule
     * @return
     */
    private boolean canProcessAgain(final ShipmentSession session, final TemperatureRule rule) {
        return rule.getMaxRateMinutes() != null && getLastAlertTime(session, rule) != null;
    }

    /**
     * @param session
     * @param rule
     * @return
     */
    private Date getLastAlertTime(final ShipmentSession session, final TemperatureRule rule) {
        final String key = createAlertTimeKey(rule);

        final String timeStr = session.getShipmentProperty(key);
        if (timeStr == null) {
            return null;
        }

        Date lastProcessed;
        try {
            lastProcessed = createTimeFormat().parse(timeStr);
        } catch (final ParseException e) {
            throw new RuntimeException("Failed to parse the time string " + timeStr);
        }

        return lastProcessed;
    }

    /**
     * @param session
     * @param rule
     */
    private void clearNotCummulativeDates(final ShipmentSession session, final TemperatureRule rule) {
        if (!rule.isCumulativeFlag()) {
            //clear first issue date
            final Map<String, String> props = session.getTemperatureAlerts()
                    .getProperties();
            props.remove(createStartIssueKey(rule));
        }
    }
    /**
     * @param session
     * @param rule
     */
    private void clearCummulativeDates(final ShipmentSession session, final TemperatureRule rule) {
        if (rule.isCumulativeFlag()) {
            final Map<String, String> props = session.getTemperatureAlerts()
                    .getProperties();

            //process cumulative rule
            final String cumulativeTotalKey = createCumulativeTotalKey(rule);
            props.remove(cumulativeTotalKey);
        }
    }
    /**
     * @return time format.
     */
    private DateFormat createTimeFormat() {
        return DateTimeUtils.createDateFormat("yyyy-MM-dd'T'HH:mm:ss",
                Language.English, TimeZone.getDefault());
    }

    /**
     * @param rule temperature rule.
     * @return key for shipment properties.
     */
    private String createAlertTimeKey(final TemperatureRule rule) {
        return NAME + "_" + rule.getType() + "_" + rule.getId() + "-time";
    }
    /**
     * @param rule
     * @return
     */
    private String createCumulativeTotalKey(final TemperatureRule rule) {
        return createBaseRuleKey(rule) + "_total";
    }
    /**
     * @param rule
     * @return
     */
    private String createStartIssueKey(final TemperatureRule rule) {
        final String ruleKey = createBaseRuleKey(rule);
        return ruleKey + "_start";
    }

    /**
     * @param rule
     * @return
     */
    private static String createBaseRuleKey(final TemperatureRule rule) {
        return NAME + "_" + rule.getType() + "_" + rule.getId();
    }

    /**
     * @param rule rule.
     * @param event event.
     * @return temperature alert.
     */
    public static TemperatureAlert createAlert(final TemperatureRule rule, final TrackerEvent event) {
        final TemperatureAlert alert = new TemperatureAlert();
        defaultAssign(event, alert);
        alert.setMinutes(rule.getTimeOutMinutes());
        alert.setCumulative(rule.isCumulativeFlag());
        alert.setTemperature(rule.getTemperature());
        alert.setType(rule.getType());
        alert.setRuleId(rule.getId());
        return alert;
    }
    /**
     * @param prev
     * @param event
     * @return
     */
    protected double getAverageTemparature(final TrackerEvent prev,
            final TrackerEvent event) {
        return prev == null
                ? event.getTemperature()
                : (event.getTemperature() + prev.getTemperature()) / 2;
    }
    /**
     * @param rule
     * @param t
     * @return
     */
    public static boolean isMatches(final TemperatureRule rule, final double t) {
        switch (rule.getType()) {
            case Cold:
            case CriticalCold:
                if (t <= rule.getTemperature()) {
                    return true;
                }
                break;
            case Hot:
            case CriticalHot:
                if (t >= rule.getTemperature()) {
                    return true;
                }
                break;
                default:
        }

        return false;
    }
}
