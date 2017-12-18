/**
 *
 */
package com.visfresh.mock;

import java.util.Date;

import org.springframework.stereotype.Component;

import com.visfresh.entities.Shipment;
import com.visfresh.services.SiblingDetectionService;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class MockSiblingDetectionService implements SiblingDetectionService {
    /**
     * Default constructor.
     */
    public MockSiblingDetectionService() {
        super();
    }
    /* (non-Javadoc)
     * @see com.visfresh.services.SiblingDetectionService#scheduleSiblingDetection(com.visfresh.entities.Shipment, java.util.Date)
     */
    @Override
    public void scheduleSiblingDetection(final Shipment s, final Date scheduleDate) {
    }
}
