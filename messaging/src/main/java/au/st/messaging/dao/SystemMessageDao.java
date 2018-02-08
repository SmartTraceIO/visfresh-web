/**
 *
 */
package au.st.messaging.dao;

import java.util.Date;
import java.util.List;
import java.util.Set;

import au.st.messaging.SystemMessage;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public interface SystemMessageDao {
    /**
     * @param listenTypes set of listen types.
     * @return true if has message of given type.
     */
    boolean hasMessages(Set<String> listenTypes);
    /**
     * @param dispatcher dispatcher ID.
     * @param listenTypes set of listener types.
     * @param date ready date.
     * @param batchSize batch size.
     * @return number of locked messages.
     */
    int lockReadyMessages(String dispatcher, Set<String> listenTypes, Date date, int batchSize);
    /**
     * @param dispatcher dispatcher ID.
     * @return list of system messages locked by given dispatcher.
     */
    List<SystemMessage> getMessages(String dispatcher);
    /**
     * Unlocks message.
     * @param m message to unlock.
     */
    void unlock(SystemMessage m);
    /**
     * @param m message to delete.
     */
    void delete(SystemMessage m);
    /**
     * @param m message.
     * @param retryOn retry time.
     */
    void unlockAndRetryOn(SystemMessage m, Date retryOn);
    /**
     * @param processor processor ID.
     * @param group group of messages.
     * @param readyOn ready date.
     * @param batch max number of messages to lock.
     * @return number of locked messages.
     */
    int lockGroupMessages(String processor, String group, Date readyOn, int batch);
    /**
     * @param date ready date.
     * @param maxGroupCount TODO
     * @return set of groups of ready messages.
     */
    Set<String> getReadyUnlockingGroups(Date date, int maxGroupCount);
}
