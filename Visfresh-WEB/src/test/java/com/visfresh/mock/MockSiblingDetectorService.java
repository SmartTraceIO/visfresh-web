/**
 *
 */
package com.visfresh.mock;

import org.springframework.stereotype.Component;

import com.visfresh.mpl.services.ShipmentSiblingServiceImpl;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class MockSiblingDetectorService extends ShipmentSiblingServiceImpl {
    /**
     * Default constructor.
     */
    public MockSiblingDetectorService() {
        super();
    }
}
