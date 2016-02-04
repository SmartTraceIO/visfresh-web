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
    ArrivalEstimation estimateArrivalDate(Shipment s, Location currentLocation, Date startTime, Date currentTime);
}
