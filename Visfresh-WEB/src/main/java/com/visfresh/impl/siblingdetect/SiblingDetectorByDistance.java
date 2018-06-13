/**
 *
 */
package com.visfresh.impl.siblingdetect;

import java.util.Date;

import com.visfresh.io.TrackerEventDto;
import com.visfresh.utils.LocationUtils;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class SiblingDetectorByDistance extends StatefullSiblingDetector {
    public static final double MAX_DISTANCE_AVERAGE = 3000; //meters
    private double maxDistanceAverage = MAX_DISTANCE_AVERAGE;

    private int count;
    private double summ;
    private double summ2;

    private TrackerEventDto e1Prev;
    private TrackerEventDto e2Prev;
    protected final CalculationDirection direction;

    /**
     * @param direction direction.
     */
    public SiblingDetectorByDistance(final CalculationDirection direction) {
        super();
        this.direction = direction;
    }

    /* (non-Javadoc)
     * @see com.visfresh.impl.siblingdetect.StateFullSiblingDetector#doNext(com.visfresh.io.TrackerEventDto, com.visfresh.io.TrackerEventDto)
     */
    @Override
    protected void doNext(final TrackerEventDto e1Orig, final TrackerEventDto e2Orig) {
        final TrackerEventDto e1 = isNullOrNullLocation(e1Orig) ? null : e1Orig;
        final TrackerEventDto e2 = isNullOrNullLocation(e2Orig) ? null : e2Orig;

        if (e1 != null && e2 != null) {
            doMeasure(getDistance(e1, e2));
            e1Prev = e1;
            e2Prev = e2;
        } else if (e1 != null) {
            if (between(e2Prev, e1Prev, e1)) {
                doMeasure(getDistance(e2Prev, e1Prev, e1));
            }
            e1Prev = e1;
        } else if (e2 != null) {
            if (between(e1Prev, e2Prev, e2)) {
                doMeasure(getDistance(e1Prev, e2Prev, e2));
            }
            e2Prev = e2;
        }
    }

    /**
     * @param e event.
     * @return true if event has null location.
     */
    private boolean isNullOrNullLocation(final TrackerEventDto e) {
        return e == null || e.getLatitude() == null || e.getLongitude() == null;
    }

    /**
     * @param distance next calculated distance.
     */
    private void doMeasure(final double distance) {
        count++;
        summ+= distance;
        summ2 += distance * distance;

        if (count > 1 && direction == CalculationDirection.RightToLeft) {
            final double avg = summ / count;
            final double d = Math.sqrt((summ2 - 2 * avg * summ
                    + count * avg * avg) / (count - 1));

            if (avg - d > getMaxDistanceAverage()) {
                setState(State.NotSiblings);
            } else if (avg + d < getMaxDistanceAverage()) {
                setState(State.Siblings);
            }
        }
    }

    /**
     * @param e
     * @param eFirst
     * @param eLast
     * @return
     */
    private boolean between(final TrackerEventDto e, final TrackerEventDto eFirst, final TrackerEventDto eLast) {
        if (e == null || eFirst == null || eLast == null) {
            return false;
        }
        return !direction.isBefore(e, eFirst) && !direction.isBefore(eLast, e);
    }

    /**
     * @param e event.
     * @param me1 first master event.
     * @param me2 second master event.
     * @return distance between given event and the sub path
     */
    private double getDistance(final TrackerEventDto e, final TrackerEventDto me1,
            final TrackerEventDto me2) {
        //not ordinary situations
        final Date mt1 = me1.getTime();
        final Date mt2 = me2.getTime();

        if (mt1.equals(mt2)) {
            return getDistance(e, me1);
        }

        final Date t = e.getTime();
        final long dt = mt2.getTime() - mt1.getTime();

        final double lat;
        final double lon;

        if (dt == 0) {
            lat = me1.getLatitude();
            lon = me1.getLongitude();
        } else {
            final double delta = (double) (t.getTime() - mt1.getTime()) / dt;

            lat = me1.getLatitude() + delta * (me2.getLatitude() - me1.getLatitude());
            lon = me1.getLongitude() + delta * (me2.getLongitude() - me1.getLongitude());
        }

        return getDistanceMeters(e.getLatitude(), e.getLongitude(), lat, lon);
    }
    /**
     * @param e1 first tracker event.
     * @param e2 second tracker event.
     * @return distance between two event in meters.
     */
    private double getDistance(final TrackerEventDto e1, final TrackerEventDto e2) {
        return getDistanceMeters(e1.getLatitude(), e1.getLongitude(),
                e2.getLatitude(), e2.getLongitude());
    }
    /**
     * @param lat1
     * @param lon1
     * @param lat2
     * @param lon2
     * @return
     */
    protected double getDistanceMeters(final double lat1, final double lon1,
            final double lat2, final double lon2) {
        return LocationUtils.getDistanceMeters(lat1, lon1, lat2, lon2);
    }
    /**
     * @return the maxDistanceAverage
     */
    public double getMaxDistanceAverage() {
        return maxDistanceAverage;
    }
    /**
     * @param maxDistanceAverage the maxDistanceAverage to set
     */
    public void setMaxDistanceAverage(final double maxDistanceAverage) {
        this.maxDistanceAverage = maxDistanceAverage;
    }
    /* (non-Javadoc)
     * @see com.visfresh.impl.siblingdetect.StateFullSiblingDetector#doFinish()
     */
    @Override
    protected void doFinish() {
        if (count > 0) {
            final double avg = summ / count;
            if(avg < getMaxDistanceAverage()) {
                setState(State.Siblings);
            } else {
                setState(State.NotSiblings);
            }
        } else {
            setState(State.Undefined);
        }

        count = 0;
        summ = 0.;
        summ2 = 0.;

        e1Prev = null;
        e2Prev = null;
    }
}
