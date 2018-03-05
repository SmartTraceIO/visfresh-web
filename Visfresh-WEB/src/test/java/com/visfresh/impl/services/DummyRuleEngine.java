/**
 * 
 */
package com.visfresh.impl.services;

import java.util.Date;
import java.util.List;
import java.util.Set;

import com.visfresh.entities.AlertRule;
import com.visfresh.entities.LocationProfile;
import com.visfresh.entities.Shipment;
import com.visfresh.rules.RuleContext;
import com.visfresh.rules.TrackerEventRule;
import com.visfresh.rules.state.ShipmentSession;
import com.visfresh.services.RuleEngine;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class DummyRuleEngine implements RuleEngine {

    /**
     * 
     */
    public DummyRuleEngine() {
        // TODO Auto-generated constructor stub
    }

    /* (non-Javadoc)
     * @see com.visfresh.services.RuleEngine#invokeRules(com.visfresh.rules.RuleContext)
     */
    @Override
    public void invokeRules(RuleContext context) {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see com.visfresh.services.RuleEngine#getRule(java.lang.String)
     */
    @Override
    public TrackerEventRule getRule(String name) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.visfresh.services.RuleEngine#hasRule(java.lang.String)
     */
    @Override
    public boolean hasRule(String name) {
        // TODO Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
     * @see com.visfresh.services.RuleEngine#getAlertYetFoFire(com.visfresh.entities.Shipment)
     */
    @Override
    public List<AlertRule> getAlertYetFoFire(Shipment s) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.visfresh.services.RuleEngine#getAlertFired(com.visfresh.entities.Shipment)
     */
    @Override
    public List<AlertRule> getAlertFired(Shipment s) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.visfresh.services.RuleEngine#suppressNextAlerts(com.visfresh.entities.Shipment)
     */
    @Override
    public void suppressNextAlerts(Shipment s) {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see com.visfresh.services.RuleEngine#isAlertsSuppressed(com.visfresh.entities.Shipment)
     */
    @Override
    public boolean isAlertsSuppressed(Shipment s) {
        // TODO Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
     * @see com.visfresh.services.RuleEngine#getAlertsSuppressionDate(com.visfresh.entities.Shipment)
     */
    @Override
    public Date getAlertsSuppressionDate(Shipment s) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.visfresh.services.RuleEngine#getInterimLocations(com.visfresh.entities.Shipment)
     */
    @Override
    public List<LocationProfile> getInterimLocations(Shipment s) {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.visfresh.services.RuleEngine#getRules()
     */
    @Override
    public Set<String> getRules() {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.visfresh.services.RuleEngine#updateInterimLocations(com.visfresh.rules.state.ShipmentSession, java.util.List)
     */
    @Override
    public void updateInterimLocations(ShipmentSession session, List<LocationProfile> interims) {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see com.visfresh.services.RuleEngine#updateAutodetectingEndLocations(com.visfresh.rules.state.ShipmentSession, java.util.List)
     */
    @Override
    public void updateAutodetectingEndLocations(ShipmentSession session, List<LocationProfile> to) {
        // TODO Auto-generated method stub

    }

}
