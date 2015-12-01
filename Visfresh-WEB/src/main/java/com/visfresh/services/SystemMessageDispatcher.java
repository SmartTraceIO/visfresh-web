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
    /**
     * @param type message type.
     * @param h message handler.
     */
    public abstract void setSystemMessageHandler(SystemMessageType type,
            SystemMessageHandler h);
    /**
     * @param type TODO
     * @param messagePayload TODO
     */
    public void sendSystemMessage(SystemMessageType type, String messagePayload);
}