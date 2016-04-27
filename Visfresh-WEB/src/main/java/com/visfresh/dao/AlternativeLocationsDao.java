/**
 *
 */
package com.visfresh.dao;

import com.visfresh.entities.AlternativeLocations;
import com.visfresh.entities.ShipmentBase;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public interface AlternativeLocationsDao {
    AlternativeLocations getBy(ShipmentBase s);
    void save(ShipmentBase s, AlternativeLocations locs);
}
