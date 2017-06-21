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
     * @param lockKeyPrefix lock key prefix.
     * @return true if successfully unlocked, false if is there system messages for given device.
     */
    boolean unlockIfNoMessages(String device, String lockKeyPrefix);
    /**
     * @param beforeDate start date.
     */
    void unlockAll(Date beforeDate);
}
