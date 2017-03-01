/**
 *
 */
package com.visfresh.services;

import java.util.Date;
import java.util.List;
import java.util.Set;

import com.visfresh.entities.AlertRule;
import com.visfresh.entities.LocationProfile;
import com.visfresh.entities.Shipment;
import com.visfresh.rules.RuleContext;
import com.visfresh.rules.TrackerEventRule;
import com.visfresh.rules.state.ShipmentSession;

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
     * @return rule if has rule or default rule otherwise. Can't be null
     */
    TrackerEventRule getRule(String name);
    /**
     * @param name rule name.
     * @return true if has rule with given name.
     */
    boolean hasRule(String name);
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
     * @return interim locations.
     */
    List<LocationProfile> getInterimLocations(Shipment s);
    /**
     * @return
     */
    Set<String> getRules();
    /**
     * @param session shipment session.
     * @param interims interim locations.
     */
    void updateInterimLocations(ShipmentSession session, List<LocationProfile> interims);
    /**
     * @param session shipment session.
     * @param to target locations.
     */
    void updateAutodetectingEndLocations(ShipmentSession session, List<LocationProfile> to);
}
