/**
 *
 */
package com.visfresh.controllers.session;

import com.visfresh.services.AuthToken;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public interface RestSessionListener {
    /**
     * @param token session token.
     */
    void tokenChanged(AuthToken token);
    /**
     * @param name property name.
     * @param oldValue old property value.
     * @param newValue new property value.
     */
    void propertyChanged(String name, String oldValue, String newValue);
}
