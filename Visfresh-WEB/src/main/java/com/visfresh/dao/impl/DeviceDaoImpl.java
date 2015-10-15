/**
 *
 */
package com.visfresh.dao.impl;

import org.springframework.stereotype.Component;

import com.visfresh.dao.DeviceDao;
import com.visfresh.entities.Device;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class DeviceDaoImpl extends DaoImplBase<Device, String> implements DeviceDao {
    /**
     * Default constructor.
     */
    public DeviceDaoImpl() {
        super(Device.class);
    }
}
