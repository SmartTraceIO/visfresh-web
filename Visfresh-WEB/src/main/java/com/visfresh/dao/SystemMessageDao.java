/**
 *
 */
package com.visfresh.dao;

import java.util.Date;
import java.util.List;
import java.util.Set;

import com.visfresh.entities.SystemMessage;
import com.visfresh.entities.SystemMessageType;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public interface SystemMessageDao extends DaoBase<SystemMessage, SystemMessage, Long> {
    /**
     * @param messageTypes set of message types.
     * @param processor processor.
     * @param limit max number of messages to select.
     * @param beforeDate the limit date.
     * @return
     */
    List<SystemMessage> selectMessagesForProcessing(
            Set<SystemMessageType> messageTypes, String processor, int limit, Date beforeDate);

    /**
     * @param b ascent sorting.
     * @return list of system messages.
     */
    List<SystemMessage> findTrackerEvents(boolean b);
}
