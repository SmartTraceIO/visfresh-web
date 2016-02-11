/**
 *
 */
package com.visfresh.mock;

import org.springframework.stereotype.Component;

import com.visfresh.drools.DroolsRuleEngine;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class MockRuleEngine extends DroolsRuleEngine {
    /**
     * Default constructor.
     */
    public MockRuleEngine() {
        super();
    }
}
