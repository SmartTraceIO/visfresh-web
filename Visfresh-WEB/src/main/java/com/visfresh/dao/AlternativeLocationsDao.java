/**
 *
 */
package com.visfresh.dao;

import com.visfresh.entities.AlternativeLocations;
import com.visfresh.entities.Shipment;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public interface AlternativeLocationsDao {
    public AlternativeLocations getByShipment(Shipment s);
    public void save(Shipment s, AlternativeLocations locs);
}
