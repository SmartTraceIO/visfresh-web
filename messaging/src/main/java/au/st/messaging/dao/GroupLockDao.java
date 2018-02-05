/**
 *
 */
package au.st.messaging.dao;

import java.util.Date;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public interface GroupLockDao {
    /**
     * @param processor processor ID.
     * @param group group.
     * @param unlockOn unlock date.
     * @return
     */
    boolean lockGroup(String processor, String group, Date unlockOn);
    /**
     * @param dispatcher locker ID for unlock.
     */
    void unlockGroups(String dispatcher);
    /**
     * @param group message group.
     * @param retryOn unlock time.
     */
    void setUnlockTime(String group, Date retryOn);
}
