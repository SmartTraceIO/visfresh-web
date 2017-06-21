/**
 *
 */
package com.visfresh.mock;

import org.springframework.stereotype.Component;

import com.visfresh.impl.services.DeviceCommandServiceImpl;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class MockDeviceCommandService extends DeviceCommandServiceImpl {
    /**
     * Default constructor.
     */
    public MockDeviceCommandService() {
        super();
    }
}
