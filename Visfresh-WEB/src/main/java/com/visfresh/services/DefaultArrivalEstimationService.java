/**
 *
 */
package com.visfresh.services;

import java.util.Date;

import org.springframework.stereotype.Component;

import com.visfresh.entities.Location;
import com.visfresh.entities.Shipment;
import com.visfresh.utils.LocationUtils;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class DefaultArrivalEstimationService implements
        ArrivalEstimationService {
    /**
     * Default constructor.
     */
    public DefaultArrivalEstimationService() {
        super();
    }

    /* (non-Javadoc)
     * @see com.visfresh.services.ArrivalEstimationService#estimateArrivalDate(com.visfresh.entities.Shipment, com.visfresh.entities.Location)
     */
    @Override
    public ArrivalEstimation estimateArrivalDate(final Shipment s,
            final Location currentLocation, final Date currentTime) {
        final Date startDate = s.getShipmentDate();

        if (s.getShippedFrom() != null && s.getShippedTo() != null) {
            final Location from = s.getShippedFrom().getLocation();
            final Location to = s.getShippedTo().getLocation();

            final double allPath = LocationUtils.distFrom(from.getLatitude(), from.getLongitude(),
                    to.getLatitude(), to.getLongitude());
            if (allPath == 0) {
                return new ArrivalEstimation(new Date(currentTime.getTime()), 100);
            }

            final double reminder = LocationUtils.distFrom(
                    currentLocation.getLatitude(), currentLocation.getLongitude(),
                    to.getLatitude(), to.getLongitude());
            if (allPath == reminder) {
                return null;
            }

            final long dt = (long) (reminder * (currentTime.getTime() - startDate.getTime())
                    / (allPath - reminder));
            return new ArrivalEstimation(
                    new Date(currentTime.getTime() + dt),
                    (int) Math.round((allPath - reminder) / allPath));
        }

        return new ArrivalEstimation(startDate, 0);
    }
}
