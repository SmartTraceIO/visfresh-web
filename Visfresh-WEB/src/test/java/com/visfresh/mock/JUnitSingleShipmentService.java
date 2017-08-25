/**
 *
 */
package com.visfresh.mock;

import org.springframework.stereotype.Component;

import com.visfresh.impl.services.SingleShipmentServiceImpl;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class JUnitSingleShipmentService extends SingleShipmentServiceImpl {
    /**
     * Default constructor.
     */
    public JUnitSingleShipmentService() {
        super();
    }
}
