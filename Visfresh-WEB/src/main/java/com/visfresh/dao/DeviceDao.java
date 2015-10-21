/**
 *
 */
package com.visfresh.dao;

import java.util.List;

import com.visfresh.entities.Device;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public interface DeviceDao extends DaoBase<Device, String> {

    /**
     * @param imei device IMEI.
     * @return list of all registered devices by given IMEI.
     */
    List<Device> findAllByImei(String imei);
}
