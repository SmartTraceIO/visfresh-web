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
public interface DispatcherIdDao {
    /**
     * @param newId handler ID to save.
     * @param expiredOn TODO
     * @return true if saved, or false if already exists handler with given ID.
     */
    boolean saveHandlerId(String newId, Date expiredOn);
    /**
     * @param id handler ID.
     * @param expiredTime expired time.
     */
    void updateExpiedTime(String id, Date expiredTime);
    /**
     * @param id dispatcher ID.
     */
    void delete(String id);
    /**
     * @param date date.
     * @return set of dispatcher ID.
     */
    Set<String> getExpiredDispatcherIds(Date date);
}
