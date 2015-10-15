/**
 *
 */
package com.visfresh.dao.impl;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.visfresh.dao.ShipmentDao;
import com.visfresh.entities.Shipment;
import com.visfresh.entities.ShipmentData;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class ShipmentDaoImpl extends DaoImplBase<Shipment, Long> implements ShipmentDao {
    /**
     * Default constructor.
     */
    public ShipmentDaoImpl() {
        super(Shipment.class);
    }
    /* (non-Javadoc)
     * @see com.visfresh.dao.ShipmentDao#getShipmentData(java.util.Date, java.util.Date, java.lang.String)
     */
    @Override
    public List<ShipmentData> getShipmentData(final Date startDate, final Date endDate,
            final String onlyWithAlerts) {
        return new LinkedList<ShipmentData>();
    }
}
