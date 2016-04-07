/**
 *
 */
package com.visfresh.mock;

import org.springframework.stereotype.Component;

import com.visfresh.mpl.services.AutoStartShipmentServiceImpl;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class MockAutoStartShipmentService extends AutoStartShipmentServiceImpl {
    /**
     * Default constructor.
     */
    public MockAutoStartShipmentService() {
        super();
    }
}
