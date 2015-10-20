/**
 *
 */
package com.visfresh.mpl.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import com.visfresh.entities.TrackerEvent;
import com.visfresh.services.AbstractRuleEngine;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class DefaultRuleEngine extends AbstractRuleEngine {
    /**
     * @param env
     */
    @Autowired
    public DefaultRuleEngine(final Environment env) {
        super(env);
    }

    /* (non-Javadoc)
     * @see com.visfresh.services.RuleEngine#processTrackerEvent(com.visfresh.entities.TrackerEvent)
     */
    @Override
    public void processTrackerEvent(final TrackerEvent e) {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see com.visfresh.services.AbstractRuleEngine#updateRules()
     */
    @Override
    public void updateRules() {
        // TODO Auto-generated method stub

    }

}
