/**
 *
 */
package com.visfresh.dao;

import java.util.Date;
import java.util.List;

import org.springframework.stereotype.Component;

import com.visfresh.controllers.lite.LiteShipment;
import com.visfresh.controllers.lite.LiteShipmentResult;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public interface LiteShipmentDao {
    /**
     * @param company company.
     * @param sorting sorting.
     * @param filter filter.
     * @param page page.
     * @return
     */
    public LiteShipmentResult getShipments(final Long company, final Sorting sorting,
            final Filter filter, final Page page);
    /**
     * @param company company.
     * @param lat latitude of coordinates.
     * @param lon longitude of coordinates.
     * @param radius radius of location.
     * @param startDate start date. Can be null.
     * @return list of shipments near given location.
     */
    public List<LiteShipment> getShipmentsNearby(Long company, double lat, double lon, int radius, Date startDate);
}
