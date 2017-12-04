/**
 *
 */
package com.visfresh.dao;

import java.util.List;

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
     * @param shipments list of shipments.
     * @return list of statistics for given shipments.
     */
    List<ShipmentStatistics> getStatistics(List<Shipment> shipments);
    /**
     * @param stats shipment shipment statistics.
     */
    void saveStatistics(ShipmentStatistics stats);
}
