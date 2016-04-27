/**
 *
 */
package com.visfresh.services;

import java.util.Date;
import java.util.List;

import com.visfresh.entities.AlertRule;
import com.visfresh.entities.LocationProfile;
import com.visfresh.entities.Shipment;
import com.visfresh.entities.ShipmentBase;
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
    /**
     * @param s shipment
     * @return map of alert yet to fire.
     */
    List<AlertRule> getAlertYetFoFire(Shipment s);
    /**
     * @param s shipment.
     * @return alerts already fired for given shipment.
     */
    List<AlertRule> getAlertFired(Shipment s);
    /**
     * Suppress next alerts for shipment.
     * @param s the shipment.
     */
    void suppressNextAlerts(Shipment s);
    /**
     * @param s shipment.
     * @return alerts suppressed state of shipment.
     */
    boolean isAlertsSuppressed(Shipment s);
    /**
     * @param s shipment.
     * @return alerts suppression date.
     */
    Date getAlertsSuppressionDate(Shipment s);
    /**
     * @param s shipment.
     * @param stops interim stop locations.
     */
    void setInterimLocations(ShipmentBase s, List<LocationProfile> stops);
    /**
     * @param s shipment.
     * @return interim locations.
     */
    List<LocationProfile> getInterimLocations(Shipment s);
}
