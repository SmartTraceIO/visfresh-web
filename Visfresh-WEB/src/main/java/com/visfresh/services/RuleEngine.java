/**
 *
 */
package com.visfresh.services;

import com.visfresh.entities.TrackerEvent;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public interface RuleEngine {
    void processTrackerEvent(TrackerEvent e);
    abstract void updateRules();
}
