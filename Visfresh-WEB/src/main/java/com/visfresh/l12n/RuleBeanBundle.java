/**
 *
 */
package com.visfresh.l12n;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import com.visfresh.entities.TemperatureUnits;
import com.visfresh.io.shipment.AlertRuleBean;
import com.visfresh.io.shipment.TemperatureRuleBean;
import com.visfresh.utils.LocalizationUtils;
import com.visfresh.utils.StringUtils;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class RuleBeanBundle {
    private static final String BUNDLE_NAME = "rules";

    /**
     * Default constructor.
     */
    public RuleBeanBundle() {
        super();
    }
    /**
     * @param rule rule.
     * @param units temperature units.
     * @return localized description.
     */
    public String buildDescription(final AlertRuleBean rule, final TemperatureUnits units) {
        final String str = getBundle().getString(buildKey(rule));
        return StringUtils.getMessage(str, createReplacementMap(rule, units));
    }
    /**
     * @param rule the rule.
     * @return bundle key.
     */
    private String buildKey(final AlertRuleBean rule) {
        final StringBuilder sb = new StringBuilder(rule.getType().name());
        if (rule instanceof TemperatureRuleBean) {
            final TemperatureRuleBean tr = (TemperatureRuleBean) rule;
            if (tr.hasCumulativeFlag()) {
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
    private Map<String, String> createReplacementMap(final AlertRuleBean rule,
            final TemperatureUnits units) {
        final Map<String, String> map =new HashMap<>();

        map.put("type", buildKey(rule));
        if (rule instanceof TemperatureRuleBean) {
            final TemperatureRuleBean tr = (TemperatureRuleBean) rule;
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
    public String getAlertsFiredString(final List<TemperatureRuleBean> alertsFired, final TemperatureUnits units) {
        final List<String> alerts = new LinkedList<>();
        for (final TemperatureRuleBean alert: alertsFired) {
            alerts.add(buildDescription(alert, units));
        }
        return StringUtils.combine(alerts, ",");
    }
}
