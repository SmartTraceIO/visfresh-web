/**
 *
 */
package com.visfresh.dao;

import java.util.List;
import java.util.Set;

import com.visfresh.entities.SystemMessage;
import com.visfresh.entities.SystemMessageType;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public interface SystemMessageDao extends DaoBase<SystemMessage, Long> {
    /**
     * @param messageTypes
     * @param processor
     * @return
     */
    List<SystemMessage> selectMessagesForProcessing(
            Set<SystemMessageType> messageTypes, String processor);
}
