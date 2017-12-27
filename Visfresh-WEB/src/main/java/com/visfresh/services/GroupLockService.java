/**
 *
 */
package com.visfresh.services;

import java.util.Date;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public interface GroupLockService {
    /**
     * @param group group ID.
     * @param lockerId locker ID.
     * @return true if the device is locked for given locker.
     */
    boolean lockGroup(String group, String lockerId);
    /**
     * @param group group ID.
     * @param lockerId locker ID.
     */
    void unlock(String group, String lockerId);
    /**
     * @param group group ID.
     * @param lockerId locker ID.
     * @param unlockOn unlock date.
     */
    void setUnlockOn(String group, String lockerId, Date unlockOn);
}
