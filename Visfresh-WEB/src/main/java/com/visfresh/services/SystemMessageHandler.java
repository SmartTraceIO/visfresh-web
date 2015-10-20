/**
 *
 */
package com.visfresh.services;

import com.visfresh.entities.SystemMessage;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public interface SystemMessageHandler {
    /**
     * @param msg system message.
     * @throws RetryableException
     */
    void handle(SystemMessage msg) throws RetryableException;
}
