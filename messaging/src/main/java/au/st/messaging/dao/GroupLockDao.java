/**
 *
 */
package au.st.messaging.dao;

import java.util.Date;
import java.util.Set;

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
     * @param dispatcher dispatcher ID.
     */
    void unlockAllForLock(String dispatcher);
    /**
     * @param date expiration date.
     * @return set of groups.
     */
    Set<String> getGroupsWithExpiredLock(Date date);
    /**
     * @param group group.
     */
    void unlockGroup(String group);
}
