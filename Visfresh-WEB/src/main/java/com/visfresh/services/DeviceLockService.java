/**
 *
 */
package com.visfresh.services;

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
     * @param imei device IMEI.
     * @return true if the device is successfully unlocked, false if there is
     * incomming messages for given device and should be processed them next.
     */
    boolean unlock(String imei);
}
