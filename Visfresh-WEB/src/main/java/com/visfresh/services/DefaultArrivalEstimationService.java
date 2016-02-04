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
     * 60 km/h as meters/milliseconds
     */
    private static final double V_60_KM_H = 60. * 1000 / (60 * 60 * 1000);
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
            final Location currentLocation, final Date startDate, final Date currentTime) {
        final long dt = currentTime.getTime() - startDate.getTime();
        if (dt > 0 && s.getShippedFrom() != null && s.getShippedTo() != null) {
            final Location from = s.getShippedFrom().getLocation();
            final Location to = s.getShippedTo().getLocation();

            //calculate speed.
            final double pathDone = LocationUtils.getDistanceMeters(
                    currentLocation.getLatitude(), currentLocation.getLongitude(),
                    from.getLatitude(), from.getLongitude());

            //v should be not less then 60km/h
            final double v = Math.max(V_60_KM_H, pathDone / dt);

            //calculate reminder distance
            double reminder = LocationUtils.getDistanceMeters(
                    currentLocation.getLatitude(), currentLocation.getLongitude(),
                    to.getLatitude(), to.getLongitude());
            reminder = Math.max(reminder - s.getShippedTo().getRadius(), 0);
            if (reminder == 0) {
                return new ArrivalEstimation(new Date(currentTime.getTime()), 100);
            }

            final int percentageComplete = (int) Math.round(100. * (pathDone / (reminder + pathDone)));
            return new ArrivalEstimation(
                    new Date(currentTime.getTime() + (long) (reminder / v)),
                    percentageComplete);
        }

        return new ArrivalEstimation(startDate, 0);
    }
}
