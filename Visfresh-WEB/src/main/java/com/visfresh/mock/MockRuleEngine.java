/**
 *
 */
package com.visfresh.mock;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import com.visfresh.entities.Shipment;
import com.visfresh.entities.TrackerEvent;
import com.visfresh.services.AbstractRuleEngine;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class MockRuleEngine extends AbstractRuleEngine {
    /**
     * @param env
     */
    @Autowired
    public MockRuleEngine(final Environment env) {
        super(env);
    }

    /* (non-Javadoc)
     * @see com.visfresh.services.RuleEngine#processTrackerEvent(com.visfresh.entities.TrackerEvent)
     */
    @Override
    public void processTrackerEvent(Shipment shipment, final TrackerEvent e) {
    }

    /* (non-Javadoc)
     * @see com.visfresh.services.AbstractRuleEngine#updateRules()
     */
    @Override
    public void updateRules() {
    }
}
