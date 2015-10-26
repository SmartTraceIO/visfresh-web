/**
 *
 */
package com.visfresh.drools;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Component;

import com.visfresh.opengts.DefaultOpenJtsFacade;
import com.visfresh.services.OpenJtsFacade;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
@ComponentScan(basePackageClasses = {DefaultOpenJtsFacade.class})
public class OpenJtsRule implements TrackerEventRule {
    /**
     * Rule name.
     */
    public static final String NAME = "OpenJts";
    @Autowired
    private OpenJtsFacade openJtsFacade;
    @Autowired
    private DroolsRuleEngine engine;
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
    public boolean accept(final TrackerEventRequest e) {
        return e.getClientProperty(this) == null;
    }
    /* (non-Javadoc)
     * @see com.visfresh.drools.TrackerEventRule#handle(com.visfresh.drools.TrackerEventRequest)
     */
    @Override
    public boolean handle(final TrackerEventRequest req) {
        //mark request as processed.
        req.putClientProperty(this, Boolean.TRUE);
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
