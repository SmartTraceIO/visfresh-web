/**
 *
 */
package com.visfresh.io;

import com.visfresh.entities.Device;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public interface DeviceResolver {
    Device getDevice(String id);
}
