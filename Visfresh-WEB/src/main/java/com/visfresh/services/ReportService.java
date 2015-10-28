/**
 *
 */
package com.visfresh.services;

import java.util.Date;

import com.visfresh.io.SingleShipmentDto;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public interface ReportService {
    /**
     * @param fromDate from date.
     * @param toDate to date.
     * @param shipment shipment.
     * @return single shipment data transfer object.
     */
    SingleShipmentDto getSingleShipment(Date fromDate, Date toDate, Long shipment);

}
