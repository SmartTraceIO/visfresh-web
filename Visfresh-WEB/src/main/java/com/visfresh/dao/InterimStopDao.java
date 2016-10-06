/**
 *
 */
package com.visfresh.dao;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.visfresh.entities.InterimStop;
import com.visfresh.entities.Shipment;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public interface InterimStopDao {
    List<InterimStop> getByShipment(Shipment s);
    void save(Shipment s, InterimStop locs);
    void updateTime(Long id, int minutes);
    Map<Long, List<InterimStop>> getByShipmentIds(Collection<Long> ids);
    void delete(Shipment shipment, InterimStop stp);
    InterimStop findOne(Shipment shipment, Long id);
}
