/**
 *
 */
package com.visfresh.services;

import java.util.Date;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public interface DeviceLockService {
    /**
     * @param imei device IMEI.
     * @param lockerId locker ID.
     * @return true if the device is locked for given locker.
     */
    boolean lockDevice(String imei, String lockerId);
    /**
     * @param device device IMEI.
     * @param lockerId locker ID.
     */
    void unlock(String device, String lockerId);
    /**
     * @param device device IMEI.
     * @param lockerId locker ID.
     * @param unlockOn unlock date.
     */
    void setUnlockOn(String device, String lockerId, Date unlockOn);
}
