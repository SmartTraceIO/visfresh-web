/**
 *
 */
package com.visfresh.dao;

import com.visfresh.entities.Device;
import com.visfresh.rules.DeviceState;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public interface DeviceDao extends EntityWithCompanyDaoBase<Device, String> {
    /**
     * Is equivalent of find by ID.
     * @param imei device IMEI.
     * @return list of all registered devices by given IMEI.
     */
    Device findByImei(String imei);
    /**
     * @param imei device IMEI.
     * @return device state.
     */
    DeviceState getState(String imei);
    /**
     * @param imei device IMEI.
     * @param state device state.
     */
    void saveState(String imei, DeviceState state);
}
