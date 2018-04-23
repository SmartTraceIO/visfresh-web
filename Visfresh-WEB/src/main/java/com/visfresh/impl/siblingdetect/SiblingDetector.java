/**
 *
 */
package com.visfresh.impl.siblingdetect;

import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import com.visfresh.io.TrackerEventDto;
import com.visfresh.utils.LocationUtils;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class SiblingDetector {
    /**
     * Minimal path for siblings.
     */
    public static final int MIN_PATH = 25000; // meters
    public static final double MAX_DISTANCE_AVERAGE = 3000; //meters

    private final CalculationDirection direction;
    private int minPathMeters = MIN_PATH;
    private double maxDistanceAverage = MAX_DISTANCE_AVERAGE;

    /**
     * Default constructor.
     */
    public SiblingDetector(final CalculationDirection direction) {
        super();
        this.direction = direction;
    }
    /**
     * @param originE1
     * @param originE2
     * @return true if siblings.
     */
    public boolean isSiblings(final List<TrackerEventDto> originE1, final List<TrackerEventDto> originE2) {
        if (originE1.isEmpty() || originE2.isEmpty()) {
            return false;
        }

        final List<TrackerEventDto> e1 = direction.createListForProcess(new LinkedList<>(originE1));
        final List<TrackerEventDto> e2 = direction.createListForProcess(new LinkedList<>(originE2));

        //1. ignore events before first
        removeEventsBefore(e2, e1.get(0));
        if (e2.isEmpty()) {
            return false;
        }

        //get events of given tracker after the intersecting time
        cutEventsAfterDate(e1, e2.get(e2.size() - 1));

        final boolean isSiblings = isSiblingsByGateway(e1, e2)
                || isSiblingsByDistance(e1, e2, getMaxDistanceAverage());

        //check given tracker lives the sibling area
        return isSiblings
                && isPathNotLessThen(e1, getMinPathMeters())
                && isPathNotLessThen(e2, getMinPathMeters());
    }
    /**
     * @param e1 first list of events
     * @param e2 second list of events
     * @return
     */
    private boolean isSiblingsByGateway(final List<TrackerEventDto> e1, final List<TrackerEventDto> e2) {
        final Iterator<TrackerEventDto> iter1 = e1.iterator();
        final Iterator<TrackerEventDto> iter2 = e2.iterator();

        final Set<String> g1 = new HashSet<>();
        final Set<String> g2 = new HashSet<>();
        while (iter1.hasNext() && iter2.hasNext()) {
            final TrackerEventDto n1 = iter1.next();
            final TrackerEventDto n2 = iter2.next();

            g1.add(n1.getGateway());
            g2.add(n2.getGateway());

            if (n1.getGateway() != null && g2.contains(n1.getGateway())) {
                return true;
            }
            if (n2.getGateway() != null && g1.contains(n2.getGateway())) {
                return true;
            }
        }

        //process reminders
        while (iter1.hasNext()) {
            final TrackerEventDto n1 = iter1.next();
            if (n1.getGateway() != null && g2.contains(n1.getGateway())) {
                return true;
            }
        }

        //process reminders
        while (iter2.hasNext()) {
            final TrackerEventDto n2 = iter2.next();
            if (n2.getGateway() != null && g1.contains(n2.getGateway())) {
                return true;
            }
        }

        return false;
    }
    /**
     * @param e1 first list of events
     * @param e2 second list of events
     * @param maxAvg max acceptable average distance.
     * @return
     */
    private boolean isSiblingsByDistance(
            final List<TrackerEventDto> e1, final List<TrackerEventDto> e2, final double maxAvg) {
        //calculate the distance in intersected time
        int count = 0;
        final double maxSumm = maxAvg * e1.size(); // max acceptable summ
        double summ = 0;

        final Iterator<TrackerEventDto> iter1 = e1.iterator();
        final Iterator<TrackerEventDto> iter2 = e2.iterator();

        TrackerEventDto before = iter2.next();
        while (iter1.hasNext()) {
            final TrackerEventDto e = iter1.next();

            if (!direction.isBefore(e, before)) {
                while (iter2.hasNext()) {
                    final TrackerEventDto after = iter2.next();
                    if (!direction.isBefore(after, e)) {
                        summ += getDistance(e, before, after);
                        count++;

                        //if max possible avg is more than avg limit, should return the max distance
                        //as for not siblings
                        if (summ >= maxSumm) {
                            return false;
                        }
                        before = after;
                        break;
                    }
                    if (!direction.isBefore(after, e)) {
                        before = after;
                    }
                }
            }
        }

        if (count == 0) {
            return false;
        }

        return summ / count < maxAvg;
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
     * @param events events.
     * @param e event.
     */
    private void removeEventsBefore(final List<TrackerEventDto> events,
            final TrackerEventDto e) {
        final Iterator<TrackerEventDto> iter = events.iterator();
        while (iter.hasNext()) {
            if (direction.isBefore(iter.next(), e)) {
                iter.remove();
            } else {
                break;
            }
        }
    }

    /**
     * @param events
     * @param e
     * @return
     */
    private List<TrackerEventDto> cutEventsAfterDate(final List<TrackerEventDto> events,
            final TrackerEventDto e) {
        final List<TrackerEventDto> list = new LinkedList<>();
        final Iterator<TrackerEventDto> iter = events.iterator();
        while (iter.hasNext()) {
            final TrackerEventDto event = iter.next();
            if (direction.isBefore(e, event)) {
                iter.remove();
                list.add(event);
            }
        }

        return list;
    }

    /**
     * @param events
     * @param minPath
     * @return
     */
    private boolean isPathNotLessThen(final List<TrackerEventDto> events, final int minPath) {
        if (events.size() == 0) {
            return false;
        }

        final LinkedList<TrackerEventDto> list = new LinkedList<>(events);
        TrackerEventDto e;
        while (list.size() > 0) {
            e = list.remove(0);

            final Iterator<TrackerEventDto> iter = list.descendingIterator();
            while (iter.hasNext()) {
                if (getDistance(e, iter.next()) >= minPath) {
                    return true;
                }
            }
        }

        return false;
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
     * @return the minPathMeters
     */
    public int getMinPathMeters() {
        return minPathMeters;
    }
    /**
     * @return the maxDistanceAverage
     */
    public double getMaxDistanceAverage() {
        return maxDistanceAverage;
    }
    /**
     * @param minPathMeters the minPathMeters to set
     */
    public void setMinPathMeters(final int minPathMeters) {
        this.minPathMeters = minPathMeters;
    }
    /**
     * @param maxDistanceAverage the maxDistanceAverage to set
     */
    public void setMaxDistanceAverage(final double maxDistanceAverage) {
        this.maxDistanceAverage = maxDistanceAverage;
    }
}
