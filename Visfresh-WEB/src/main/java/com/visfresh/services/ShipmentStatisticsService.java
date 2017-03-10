/**
 *
 */
package com.visfresh.services;

import com.visfresh.entities.Shipment;
import com.visfresh.rules.state.ShipmentStatistics;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public interface ShipmentStatisticsService {
    /**
     * @param s the shipment.
     * @return shipment statistics.
     */
    ShipmentStatistics calculate(Shipment s);
}
