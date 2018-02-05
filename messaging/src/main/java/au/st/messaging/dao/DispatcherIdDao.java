/**
 *
 */
package au.st.messaging.dao;

import java.util.Date;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public interface DispatcherIdDao {
    /**
     * @param newId handler ID to save.
     * @return true if saved, or false if already exists handler with given ID.
     */
    boolean saveHandlerId(String newId);
    /**
     * @param id handler ID.
     * @param expiredTime expired time.
     */
    void updateHandlerActivity(String id, Date expiredTime);
    /**
     * Deletes all handler ID with expired time before given.
     * @param expiredOn expired time.
     */
    void deleteExpiredId(Date expiredOn);
    /**
     * @param id dispatcher ID.
     */
    void delete(String id);
}
