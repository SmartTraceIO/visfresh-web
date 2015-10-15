/**
 *
 */
package com.visfresh.dao.impl;

import org.springframework.stereotype.Component;

import com.visfresh.dao.DeviceCommandDao;
import com.visfresh.entities.DeviceCommand;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class DeviceCommandDaoImpl extends DaoImplBase<DeviceCommand, Long> implements DeviceCommandDao {
    /**
     * Default constructor.
     */
    public DeviceCommandDaoImpl() {
        super(DeviceCommand.class);
    }
}
