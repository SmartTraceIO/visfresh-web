/**
 *
 */
package com.visfresh.rules;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.visfresh.services.OpenJtsFacade;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class OpenJtsRule implements TrackerEventRule {
    /**
     * Rule name.
     */
    public static final String NAME = "OpenJts";
    @Autowired
    private OpenJtsFacade openJtsFacade;
    @Autowired
    private AbstractRuleEngine engine;

    /**
     * Is enabled flag (only for unit tests.
     */
    private boolean isEnabled;

    /**
     * Default constructor.
     */
    public OpenJtsRule() {
        super();
    }

    @PostConstruct
    public void initalize() {
        engine.setRule(NAME, this);
    }

    /* (non-Javadoc)
     * @see com.visfresh.drools.TrackerEventRule#accept(com.visfresh.drools.TrackerEventRequest)
     */
    @Override
    public boolean accept(final RuleContext e) {
        return !e.isProcessed(this);
    }
    /* (non-Javadoc)
     * @see com.visfresh.drools.TrackerEventRule#handle(com.visfresh.drools.TrackerEventRequest)
     */
    @Override
    public boolean handle(final RuleContext req) {
        //mark request as processed.
        req.setProcessed(this);
        if (isEnabled) {
            openJtsFacade.addTrackerEvent(req.getEvent().getShipment(), req.getEvent());
        }
        return false;
    }
    /**
     * @return the isEnabled
     */
    public boolean isEnabled() {
        return isEnabled;
    }
    /**
     * @param isEnabled the isEnabled to set
     */
    public void setEnabled(final boolean isEnabled) {
        this.isEnabled = isEnabled;
    }
}
