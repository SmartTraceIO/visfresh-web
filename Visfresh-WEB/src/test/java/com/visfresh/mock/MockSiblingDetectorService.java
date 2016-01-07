/**
 *
 */
package com.visfresh.mock;

import java.util.List;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.visfresh.dao.ShipmentDao;
import com.visfresh.entities.Shipment;
import com.visfresh.mpl.services.ShipmentSiblingServiceImpl;
import com.visfresh.mpl.services.siblings.TestSiblingDetector;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class MockSiblingDetectorService extends ShipmentSiblingServiceImpl {
    private TestSiblingDetector testDetector;

    @Autowired
    private ShipmentDao shipmentDao;

    /**
     * Default constructor.
     */
    public MockSiblingDetectorService() {
        super();
    }

    @PostConstruct
    public void init() {
        testDetector = new TestSiblingDetector() {
            {
                this.shipmentDao = MockSiblingDetectorService.this.shipmentDao;
            }
        };
    }
    /* (non-Javadoc)
     * @see com.visfresh.mpl.services.ShipmentSiblingServiceImpl#getSiblings(com.visfresh.entities.Shipment)
     */
    @Override
    public List<Shipment> getSiblings(final Shipment shipment) {
        return testDetector.getSiblings(shipment);
    }
    /* (non-Javadoc)
     * @see com.visfresh.mpl.services.ShipmentSiblingServiceImpl#getSiblingCount(com.visfresh.entities.Shipment)
     */
    @Override
    public int getSiblingCount(final Shipment s) {
        return testDetector.getSiblingCount(s);
    }
}
