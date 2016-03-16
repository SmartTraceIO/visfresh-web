/**
 *
 */
package com.visfresh.mpl.services.arrival;

import java.util.Date;

import com.visfresh.entities.Location;
import com.visfresh.entities.Shipment;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public interface ArrivalEstimationCalculator {
    Date estimateArrivalDate(Shipment s, Location currentLocation, Date startTime, Date currentTime);
}
