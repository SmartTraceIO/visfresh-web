/**
 *
 */
package com.visfresh.dao;

import com.visfresh.entities.Device;

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
}
