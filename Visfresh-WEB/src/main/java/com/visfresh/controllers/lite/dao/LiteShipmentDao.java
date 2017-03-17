/**
 *
 */
package com.visfresh.controllers.lite.dao;

import org.springframework.stereotype.Component;

import com.visfresh.constants.ShipmentConstants;
import com.visfresh.dao.Filter;
import com.visfresh.dao.Page;
import com.visfresh.dao.Sorting;
import com.visfresh.entities.Company;
import com.visfresh.entities.User;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class LiteShipmentDao {
    /**
     * Default constructor.
     */
    public LiteShipmentDao() {
        super();
    }

    /**
     * @param company company.
     * @param sorting sorting.
     * @param filter filter.
     * @param page page.
     * @param user
     * @return
     */
    public LiteShipmentResult getShipments(final Company company, final Sorting sorting, final Filter filter, final Page page,
            final User user) {
        //build SQL
        String selectAll = "select shipments.*"
            + " , substring(d.imei, -7, 6)"
            + " as " + ShipmentConstants.DEVICE_SN
            + " , sfrom.name as " + ShipmentConstants.SHIPPED_FROM_LOCATION_NAME
            + " , sto.name as " + ShipmentConstants.SHIPPED_TO_LOCATION_NAME
            + " , (select count(*) from alerts"
            + " al where al.shipment = shipments.id)"
            + " as " + ShipmentConstants.ALERT_SUMMARY
            + " from shipments"
            + " left outer join devices as d"
            + " on shipments.device = d.imei"
            + " left outer join locationprofiles as sfrom"
            + " on shipments.shippedfrom = sfrom.id"
            + " left outer join locationprofiles as sto"
            + " on shipments.shippedfrom = sto.id"
            ;
        if (filter != null && Boolean.TRUE.equals(filter.getFilter(ShipmentConstants.EXCLUDE_PRIOR_SHIPMENTS))) {
            selectAll += "\njoin (select max(id) as id, device as device from shipments group by device) newest on "
                    + "shipments.id = newest.id and "
                    + "shipments.device = newest.device\n";
        }



        // TODO Auto-generated method stub
        return null;
    }

}
