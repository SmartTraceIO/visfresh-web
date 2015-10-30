/**
 *
 */
package com.visfresh.services;

import com.visfresh.drools.TrackerEventRule;
import com.visfresh.entities.TrackerEvent;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public interface RuleEngine {
    /**
     * @param e tracker event.
     */
    void processTrackerEvent(TrackerEvent e);
    /**
     * @param name rule name.
     * @return rule.
     */
    TrackerEventRule getRule(String name);
}
