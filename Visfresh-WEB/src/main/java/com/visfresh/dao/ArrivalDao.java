/**
 *
 */
package com.visfresh.dao;

import java.util.List;

import com.visfresh.entities.Arrival;
import com.visfresh.entities.Shipment;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public interface ArrivalDao extends DaoBase<Arrival, Long> {
    /**
     * @param shipment shipment.
     * @return
     */
    List<Arrival> getArrivals(Shipment shipment);
    /**
     * @param shipment shipment.
     * @return arrival.
     */
    Arrival getArrival(Shipment shipment);
}
