/**
 *
 */
package com.visfresh.services;

import com.visfresh.entities.Shipment;
import com.visfresh.entities.TrackerEvent;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public interface RuleEngine {
    void processTrackerEvent(Shipment shipment, TrackerEvent e);
    abstract void updateRules();
}
