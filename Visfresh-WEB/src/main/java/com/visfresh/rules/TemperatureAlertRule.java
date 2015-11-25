/**
 *
 */
package com.visfresh.rules;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.visfresh.dao.TrackerEventDao;
import com.visfresh.entities.Alert;
import com.visfresh.entities.AlertRule;
import com.visfresh.entities.TemperatureAlert;
import com.visfresh.entities.TrackerEvent;

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
    public String getName() {
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

        final List<Alert> alerts = new LinkedList<Alert>();

        //process alert rules
        for (final AlertRule rule : event.getShipment().getAlertProfile().getAlertRules()) {

            //if rule is not already processed. Each rule should be processed one time.
            final Map<String, String> props = context.getState().getTemperatureAlerts().getProperties();
            if (!"true".equals(props.get(createProcessedKey(rule)))) {
                final boolean isCumulative = rule.isCumulativeFlag();
                if (isMatches(rule, t)) {
                    Alert a = null;
                    if (isCumulative) {
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
                    if (!isCumulative) {
                        //clear first issue date
                        props.remove(creaeStartIssueKey(rule));
                    }
                }
            }
        }

        return alerts.toArray(new Alert[alerts.size()]);
    }

    /**
     * @param rule rule.
     * @param context rule context.
     * @return alert.
     */
    private Alert processNormalRule(final AlertRule rule, final RuleContext context) {
        final Map<String, Date> dateProps = context.getState().getTemperatureAlerts()
                .getDates();

        final String startIssueKey = creaeStartIssueKey(rule);
        final Date firstIssue = dateProps.get(startIssueKey);
        if (firstIssue == null) {
            //if first issue, return immediately
            dateProps.put(startIssueKey, firstIssue);
            return null;
        }

        final TrackerEvent event = context.getEvent();
        final long total = (event.getTime().getTime() - firstIssue.getTime()) / MINUTE;
        if (total >= rule.getTimeOutMinutes()) {
            final Map<String, String> props = context.getState().getTemperatureAlerts()
                    .getProperties();

            final TemperatureAlert a = createAlert(rule, event);
            a.setMinutes((int) total);
            props.put(createProcessedKey(rule), "true");
            return a;
        }

        return null;
    }

    /**
     * @param rule rule.
     * @param context rule context.
     * @param prev previous event.
     */
    private Alert processComulativeRule(final AlertRule rule, final RuleContext context,
            final TrackerEvent prev) {
        final Map<String, String> props = context.getState().getTemperatureAlerts()
                .getProperties();

        //process cumulative rule
        final String cumulativeTotalKey = createBaseRuleKey(rule) + "_total";
        final TrackerEvent event = context.getEvent();

        //update summary
        final String totalStr = props.get(cumulativeTotalKey);
        long total = totalStr == null ? 0 : Long.parseLong(totalStr);
        total += Math.abs(event.getTime().getTime() - prev.getTime().getTime());

        if (total >= rule.getTimeOutMinutes() * 60000L) {
            final TemperatureAlert alert = createAlert(rule, event);
            alert.setMinutes((int) (total / 60000l));
            props.put(createProcessedKey(rule), "true");
            return alert;
        } else {
            props.put(cumulativeTotalKey, Long.toString(total));
        }

        return null;
    }

    /**
     * @param rule
     * @return
     */
    protected String creaeStartIssueKey(final AlertRule rule) {
        final String ruleKey = createBaseRuleKey(rule);
        return ruleKey + "_start";
    }
    /**
     * @param rule
     * @return
     */
    protected String createProcessedKey(final AlertRule rule) {
        final String ruleKey = createBaseRuleKey(rule);
        return ruleKey + "_processed";
    }

    /**
     * @param rule
     * @return
     */
    protected String createBaseRuleKey(final AlertRule rule) {
        return getName() + "_" + rule.getType() + "_" + rule.getId();
    }

    /**
     * @param rule rule.
     * @param event event.
     * @return temperature alert.
     */
    private TemperatureAlert createAlert(final AlertRule rule, final TrackerEvent event) {
        final TemperatureAlert alert = new TemperatureAlert();
        defaultAssign(event, alert);
        alert.setMinutes(rule.getTimeOutMinutes());
        alert.setCumulative(true);
        alert.setTemperature(rule.getTemperature());
        alert.setType(rule.getType());
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
    private boolean isMatches(final AlertRule rule, final double t) {
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
