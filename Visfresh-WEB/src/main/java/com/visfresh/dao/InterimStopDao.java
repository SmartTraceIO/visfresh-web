/**
 *
 */
package com.visfresh.dao;

import java.util.List;

import com.visfresh.entities.InterimStop;
import com.visfresh.entities.Shipment;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public interface InterimStopDao {
    List<InterimStop> getByShipment(Shipment s);
    void add(Shipment s, InterimStop locs);
}
