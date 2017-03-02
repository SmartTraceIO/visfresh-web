/**
 *
 */
package com.visfresh.dao;

import com.visfresh.entities.Shipment;
import com.visfresh.rules.state.ShipmentStatistics;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public interface ShipmentStatisticsDao {
    /**
     * @param shipment shipment.
     * @return shipment statistics.
     */
    ShipmentStatistics getStatistics(Shipment shipment);

    /**
     * @param stats shipment shipment statistics.
     */
    void saveStatistics(ShipmentStatistics stats);
    /**
     * clears the DAO cache
     */
    void clearCache();
}
