/**
 *
 */
package com.visfresh.dao;

import java.util.Date;
import java.util.List;

import com.visfresh.entities.Alert;
import com.visfresh.entities.Shipment;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public interface AlertDao extends DaoBase<Alert, Long> {
    /**
     * @param shipment
     * @param fromDate
     * @param toDate
     * @return
     */
    List<Alert> getAlerts(Shipment shipment, Date fromDate, Date toDate);
}
