/**
 * 
 */
package com.visfresh.services;

import com.visfresh.entities.SystemMessageType;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public interface SystemMessageDispatcher {

    /**
     * @param processor processor ID.
     * @return number of processed messages.
     */
    public abstract int processMessages(String processor);

    public abstract void setSystemMessageHandler(SystemMessageType type,
            SystemMessageHandler h);

}