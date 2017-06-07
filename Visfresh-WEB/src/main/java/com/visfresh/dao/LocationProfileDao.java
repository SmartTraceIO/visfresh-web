/**
 *
 */
package com.visfresh.dao;

import java.util.List;

import com.visfresh.entities.LocationProfile;
import com.visfresh.entities.ShortShipmentInfo;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public interface LocationProfileDao extends EntityWithCompanyDaoBase<LocationProfile, LocationProfile, Long> {
    /**
     * @param location location.
     * @return list of owner shipments which contains given location as start of end location.
     */
    List<ShortShipmentInfo> getOwnerShipments(LocationProfile location);
}
