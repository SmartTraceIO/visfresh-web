/**
 *
 */
package com.visfresh.services;

import com.visfresh.rules.RuleContext;
import com.visfresh.rules.TrackerEventRule;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public interface RuleEngine {
    /**
     * @param context rule context
     */
    void invokeRules(RuleContext context);
    /**
     * @param name rule name.
     * @return rule.
     */
    TrackerEventRule getRule(String name);
}
