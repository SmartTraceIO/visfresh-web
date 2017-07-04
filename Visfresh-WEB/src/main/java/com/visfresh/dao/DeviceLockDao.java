/**
 *
 */
package com.visfresh.dao;

import java.util.Date;
import java.util.List;

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
     * @param unluckDate start date.
     * @return number of unlucked devices.
     */
    int unlockOlder(Date unluckDate);
    /**
     * deletes all locks. For tests only. Please not use it in production because
     * the locks can be owned by different application instance, possible on remote server.
     */
    void deleteAll();
    /**
     * @param readyOn ready on date for messages.
     * @param limit select limit.
     * @return list of not locked devices.
     */
    List<String> getNotLockedDevicesWithReadyMessages(Date readyOn, int limit);
    /**
     * @param device device.
     * @param lockKey locker key.
     */
    void unlock(String device, String lockKey);
    /**
     * @param device device.
     * @param lockKey locker key.
     * @param unlockOn unlock date.
     */
    void setUnlockOn(String device, String lockKey, Date unlockOn);
}
