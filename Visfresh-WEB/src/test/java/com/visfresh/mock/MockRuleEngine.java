/**
 *
 */
package com.visfresh.mock;

import org.springframework.stereotype.Component;

import com.visfresh.mpl.ruleengine.VisfreshRuleEngine;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class MockRuleEngine extends VisfreshRuleEngine {
    /**
     * Default constructor.
     */
    public MockRuleEngine() {
        super();
    }
}
