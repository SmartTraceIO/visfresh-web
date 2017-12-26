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
    /**
     * @param messageType message type
     * @param group device IMEI as group.
     * @param readyOn ready on time.
     * @param batchLimit batch limit.
     * @return
     */
    List<SystemMessage> getMessagesForGoup(SystemMessageType messageType, String group, Date readyOn, int batchLimit);
    /**
     * Saves message only if there are not other messages for given group.
     * @param sm system messages.
     */
    void saveOnlyOneForGroup(SystemMessage sm);
}
