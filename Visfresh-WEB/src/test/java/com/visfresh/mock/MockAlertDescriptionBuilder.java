/**
 *
 */
package com.visfresh.mock;

import org.springframework.stereotype.Component;

import com.visfresh.rules.AlertDescriptionBuilder;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class MockAlertDescriptionBuilder extends AlertDescriptionBuilder {
    /**
     * Default constructor.
     */
    public MockAlertDescriptionBuilder() {
        super();
    }
}
