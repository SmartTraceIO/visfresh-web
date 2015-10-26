/**
 *
 */
package com.visfresh.mock;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.visfresh.entities.TrackerEvent;
import com.visfresh.services.RuleEngine;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class MockRuleEngine implements RuleEngine {
    /**
     * @param env
     */
    @Autowired
    public MockRuleEngine() {
        super();
    }

    /* (non-Javadoc)
     * @see com.visfresh.services.RuleEngine#processTrackerEvent(com.visfresh.entities.TrackerEvent)
     */
    @Override
    public void processTrackerEvent(final TrackerEvent e) {
    }
}
