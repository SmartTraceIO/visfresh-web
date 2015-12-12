/**
 *
 */
package com.visfresh.services;

import java.util.Date;

import com.visfresh.entities.Location;
import com.visfresh.entities.Shipment;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public interface ArrivalEstimationService {
    Date estimateArrivalDate(Shipment s, Location currentLocation, Date currentTime);
}
