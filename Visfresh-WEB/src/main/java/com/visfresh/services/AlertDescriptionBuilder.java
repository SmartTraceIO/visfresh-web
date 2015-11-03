/**
 *
 */
package com.visfresh.services;

import org.springframework.stereotype.Component;

import com.visfresh.entities.Alert;
import com.visfresh.entities.User;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class AlertDescriptionBuilder {
    /**
     * Default constructor.
     */
    public AlertDescriptionBuilder() {
        super();
    }

    public String buildDescription(final Alert alert, final User user) {
        //TODO implement
        return alert.getType().toString();
    }
}
