/**
 *
 */
package com.visfresh.dao;

import java.util.Date;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public interface DeviceLockDao {
    /**
     * @param device device.
     * @param lockKey lock key.
     * @return true if device successfully locked by given locker ID.
     */
    boolean lock(String device, String lockKey);
    /**
     * @param device device.
     * @param lockKey lock key.
     * @return true if successfully unlocked, false if is there system messages for given device.
     */
    boolean unlockIfNoMessages(String device, String lockKey);
    /**
     * @param beforeDate start date.
     * @return TODO
     */
    int unlockOlder(Date beforeDate);
    /**
     * deletes all locks. For tests only. Please not use it in production because
     * the locks can be owned by different application instance, possible on remote server.
     */
    void deleteAll();
}
