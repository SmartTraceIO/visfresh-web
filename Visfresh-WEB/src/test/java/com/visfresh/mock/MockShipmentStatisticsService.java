/**
 *
 */
package com.visfresh.mock;

import org.springframework.stereotype.Component;

import com.visfresh.mpl.services.ShipmentStatisticsServiceImpl;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class MockShipmentStatisticsService extends ShipmentStatisticsServiceImpl {
    /**
     * Default constructor.
     */
    public MockShipmentStatisticsService() {
        super();
    }

    /* (non-Javadoc)
     * @see com.visfresh.mpl.services.ShipmentStatisticsServiceImpl#initialize()
     */
    @Override
    public void initialize() {
        // nothing
    }
}
