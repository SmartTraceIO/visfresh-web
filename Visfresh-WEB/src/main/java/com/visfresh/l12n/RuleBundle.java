/**
 *
 */
package com.visfresh.l12n;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import org.springframework.stereotype.Component;

import com.visfresh.entities.AlertRule;
import com.visfresh.entities.TemperatureRule;
import com.visfresh.entities.TemperatureUnits;
import com.visfresh.utils.LocalizationUtils;
import com.visfresh.utils.StringUtils;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class RuleBundle {
    private static final String BUNDLE_NAME = "rules";

    /**
     * Default constructor.
     */
    public RuleBundle() {
        super();
    }
    /**
     * @param rule rule.
     * @param units temperature units.
     * @return localized description.
     */
    public String buildDescription(final AlertRule rule, final TemperatureUnits units) {
        final String str = getBundle().getString(buildKey(rule));
        return StringUtils.getMessage(str, createReplacementMap(rule, units));
    }
    /**
     * @param rule the rule.
     * @return bundle key.
     */
    private String buildKey(final AlertRule rule) {
        final StringBuilder sb = new StringBuilder(rule.getType().name());
        if (rule instanceof TemperatureRule) {
            final TemperatureRule tr = (TemperatureRule) rule;
            if (tr.isCumulativeFlag()) {
                sb.append(".cumulative");
            }
        }
        return sb.toString();
    }
    /**
     * @param rule the rule.
     * @param units temperature units.
     * @return map of replacements.
     */
    private Map<String, String> createReplacementMap(final AlertRule rule,
            final TemperatureUnits units) {
        final Map<String, String> map =new HashMap<>();

        map.put("type", buildKey(rule));
        if (rule instanceof TemperatureRule) {
            final TemperatureRule tr = (TemperatureRule) rule;
            //only temperature alert rules. Other should be returned before.
            map.put("temperature", LocalizationUtils.getTemperatureString(tr.getTemperature(), units));
            //append time
            map.put("ruleperiod", Integer.toString(tr.getTimeOutMinutes()));
        }

        return map;
    }
    /**
     * @return resource bundle.
     */
    private ResourceBundle getBundle() {
        return ResourceBundle.getBundle(BUNDLE_NAME, XmlControl.INSTANCE);
    }
    /**
     * @param alertsFired
     * @return
     */
    public String getAlertsFiredString(final List<TemperatureRule> alertsFired, final TemperatureUnits units) {
        final List<String> alerts = new LinkedList<>();
        for (final TemperatureRule alert: alertsFired) {
            alerts.add(buildDescription(alert, units));
        }
        return StringUtils.combine(alerts, ",");
    }
}
