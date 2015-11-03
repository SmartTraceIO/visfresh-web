/**
 *
 */
package com.visfresh.dao;

import java.util.List;

import com.visfresh.entities.Company;
import com.visfresh.entities.Device;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public interface DeviceDao extends DaoBase<Device, String> {
    /**
     * Is equivalent of find by ID.
     * @param imei device IMEI.
     * @return list of all registered devices by given IMEI.
     */
    Device findByImei(String imei);
    /**
     * @param company company.
     * @return list of devices.
     */
    List<Device> findByCompany(Company company);
}
