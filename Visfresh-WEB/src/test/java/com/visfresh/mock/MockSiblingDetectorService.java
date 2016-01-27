/**
 *
 */
package com.visfresh.mock;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.visfresh.dao.ShipmentDao;
import com.visfresh.mpl.services.ShipmentSiblingServiceImpl;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class MockSiblingDetectorService extends ShipmentSiblingServiceImpl {
    @Autowired
    private ShipmentDao shipmentDao;

    /**
     * Default constructor.
     */
    public MockSiblingDetectorService() {
        super();
    }
}
