/**
 *
 */
package com.visfresh.impl.siblingdetect;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.junit.Test;

import com.visfresh.io.TrackerEventDto;
import com.visfresh.utils.LocationUtils;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class SiblingDetectorByDistanceTest {
    private static final double MIN_PATH = 5000.; //meters

    /**
     * Default constructor.
     */
    public SiblingDetectorByDistanceTest() {
        super();
    }

    //LeftToRight
    @Test
    public void testIsSiblingsLeftToRight() {
        final SiblingDetector detector = createDetector(CalculationDirection.LeftToRight);

        //crete master event list
        final List<TrackerEventDto> masterEvents = new LinkedList<>();
        final List<TrackerEventDto> siblingEvents = new LinkedList<>();
        final List<TrackerEventDto> notSiblingEvents = new LinkedList<>();

        final double x0 = 10.;
        final double y0 = 10.;
        final long t0 = System.currentTimeMillis() - 1000000l;
        final long dt = 10 * 60 * 1000l;
        final double minPath = LocationUtils.getLongitudeDiff(y0, MIN_PATH);

        //intersected time
        final int count = (int) Math.round(minPath / 0.01) + 1;
        for (int i = 0; i < count; i++) {
            masterEvents.add(createTrackerEvent(x0 + 0.01 * i, y0 + 0.01 * i, t0 + i * dt));
            siblingEvents.add(createTrackerEvent(x0 + 0.01 * i + 0.005,
                    y0 + 0.01 * i + 0.005, t0 + i * dt + 60 * 1000l));
            notSiblingEvents.add(createTrackerEvent(x0 - 0.1 * i - 0.05, y0 - 0.1 * i - 0.05,
                    t0 + dt * i + 60 * 1000l));
        }

        assertTrue(SiblingDetectorTest.isSiblings(detector, masterEvents, siblingEvents));
        assertFalse(SiblingDetectorTest.isSiblings(detector, masterEvents, notSiblingEvents));
    }
    @Test
    public void testNotIntersectingByTimeLeftToRight() {
        final SiblingDetector detector = createDetector(CalculationDirection.LeftToRight);

        //crete master event list
        final List<TrackerEventDto> e2 = new LinkedList<>();
        final List<TrackerEventDto> e1 = new LinkedList<>();
        final double x0 = 10.;
        final double y0 = 10.;
        final long t0 = System.currentTimeMillis() - 1000000l;
        final long dt = 10 * 60 * 1000l;

        //add tracker events for master shipment
        e1.add(createTrackerEvent(x0, y0, t0));
        e1.add(createTrackerEvent(x0, y0, t0));
        e1.add(createTrackerEvent(x0, y0, t0));

        //add tracker events for given shipment
        e2.add(createTrackerEvent(x0, y0, t0 + dt));
        e2.add(createTrackerEvent(x0, y0, t0 + dt));
        e2.add(createTrackerEvent(x0, y0, t0 + dt));

        assertFalse(SiblingDetectorTest.isSiblings(detector, e2, e1));
        assertFalse(SiblingDetectorTest.isSiblings(detector, e1, e2));
    }
    @Test
    public void testIsTimeNotIntersectingLeftToRight() {
        final SiblingDetector detector = createDetector(CalculationDirection.LeftToRight);

        final double lat = 60;
        final double dlon = LocationUtils.getLongitudeDiff(
                lat, SiblingDetector.MAX_DISTANCE_AVERAGE) / 2.;
        final long min10 = 10 * 60 * 1000l;

        long t = 100 * min10;
        double lon = 10.;

        final List<TrackerEventDto> l1 = new LinkedList<>();
        l1.add(createTrackerEvent(lat, lon += dlon, t+= min10));
        l1.add(createTrackerEvent(lat, lon += dlon, t+= min10));
        l1.add(createTrackerEvent(lat, lon += dlon, t+= min10));
        l1.add(createTrackerEvent(lat, lon += dlon, t+= min10));

        final List<TrackerEventDto> l2 = new LinkedList<>();
        l2.add(createTrackerEvent(lat, lon += dlon, t+= min10));
        l2.add(createTrackerEvent(lat, lon += dlon, t+= min10));
        l2.add(createTrackerEvent(lat, lon += dlon, t+= min10));
        l2.add(createTrackerEvent(lat, lon += dlon, t+= min10));

        assertFalse(SiblingDetectorTest.isSiblings(detector, l2, l1));
        assertFalse(SiblingDetectorTest.isSiblings(detector, l1, l2));
    }
    @Test
    public void testIsTimeIntersectingLeftToRight() {
        final SiblingDetector detector = createDetector(CalculationDirection.LeftToRight);

        final double lat = 60;
        final double dlon = LocationUtils.getLongitudeDiff(
                lat, SiblingDetector.MAX_DISTANCE_AVERAGE) / 5.;
        final double minPath = LocationUtils.getLongitudeDiff(lat, MIN_PATH);
        final long min10 = 10 * 60 * 1000l;

        long t = 100 * min10;
        double lon = 10.;

        final List<TrackerEventDto> l1 = new LinkedList<>();
        final List<TrackerEventDto> l2 = new LinkedList<>();

        l1.add(createTrackerEvent(lat, lon += dlon, t+= min10));
        l1.add(createTrackerEvent(lat, lon += dlon, t+= min10));
        l1.add(createTrackerEvent(lat, lon += dlon, t+= min10));
        l1.add(createTrackerEvent(lat, lon += dlon, t+= min10));

        //intersected time
        final int count = (int) Math.round(minPath / dlon) + 1;
        for (int i = 0; i < count; i++) {
            l1.add(createTrackerEvent(lat, lon += dlon, t+= min10));
            l2.add(createTrackerEvent(lat, lon += dlon, t+= min10));
        }

        //l1 stopped l2 continued
        l2.add(createTrackerEvent(lat, lon += dlon, t+= min10));
        l2.add(createTrackerEvent(lat, lon += dlon, t+= min10));
        l2.add(createTrackerEvent(lat, lon += dlon, t+= min10));
        l2.add(createTrackerEvent(lat, lon += dlon, t+= min10));
        l2.add(createTrackerEvent(lat, lon += dlon, t+= min10));
        l2.add(createTrackerEvent(lat, lon += dlon, t+= min10));
        l2.add(createTrackerEvent(lat, lon += dlon, t+= min10));
        l2.add(createTrackerEvent(lat, lon += dlon, t+= min10));
        l2.add(createTrackerEvent(lat, lon += dlon, t+= min10));
        l2.add(createTrackerEvent(lat, lon += dlon, t+= min10));
        l2.add(createTrackerEvent(lat, lon += dlon, t+= min10));
        l2.add(createTrackerEvent(lat, lon += dlon, t+= min10));

        assertTrue(SiblingDetectorTest.isSiblings(detector, l2, l1));
    }

    //Right to Left
    @Test
    public void testIsSiblingsRightToLeft() {
        final SiblingDetector detector = createDetector(CalculationDirection.RightToLeft);

        //crete master event list
        final List<TrackerEventDto> masterEvents = new LinkedList<>();
        final List<TrackerEventDto> siblingEvents = new LinkedList<>();
        final List<TrackerEventDto> notSiblingEvents = new LinkedList<>();

        final double x0 = 10.;
        final double y0 = 10.;
        final long t0 = System.currentTimeMillis() - 1000000l;
        final long dt = 10 * 60 * 1000l;
        final double minPath = LocationUtils.getLongitudeDiff(y0, MIN_PATH);

        //intersected time
        final int count = (int) Math.round(minPath / 0.01) + 1;
        for (int i = 0; i < count; i++) {
            masterEvents.add(createTrackerEvent(x0 + 0.01 * i, y0 + 0.01 * i, t0 + i * dt));
            siblingEvents.add(createTrackerEvent(x0 + 0.01 * i + 0.005,
                    y0 + 0.01 * i + 0.005, t0 + i * dt + 60 * 1000l));
            notSiblingEvents.add(createTrackerEvent(x0 - 0.1 * i - 0.05, y0 - 0.1 * i - 0.05,
                    t0 + dt * i + 60 * 1000l));
        }

        assertTrue(SiblingDetectorTest.isSiblings(detector, masterEvents, siblingEvents));
        assertFalse(SiblingDetectorTest.isSiblings(detector, masterEvents, notSiblingEvents));
    }
    @Test
    public void testNotIntersectingByTimeRightToLeft() {
        final SiblingDetector detector = createDetector(CalculationDirection.RightToLeft);

        //crete master event list
        final List<TrackerEventDto> e2 = new LinkedList<>();
        final List<TrackerEventDto> e1 = new LinkedList<>();
        final double x0 = 10.;
        final double y0 = 10.;
        final long t0 = System.currentTimeMillis() - 1000000l;
        final long dt = 10 * 60 * 1000l;

        //add tracker events for master shipment
        e1.add(createTrackerEvent(x0, y0, t0));
        e1.add(createTrackerEvent(x0, y0, t0));
        e1.add(createTrackerEvent(x0, y0, t0));

        //add tracker events for given shipment
        e2.add(createTrackerEvent(x0, y0, t0 + dt));
        e2.add(createTrackerEvent(x0, y0, t0 + dt));
        e2.add(createTrackerEvent(x0, y0, t0 + dt));

        assertFalse(SiblingDetectorTest.isSiblings(detector, e2, e1));
        assertFalse(SiblingDetectorTest.isSiblings(detector, e1, e2));
    }
    @Test
    public void testIsTimeNotIntersectingRightToLeft() {
        final SiblingDetector detector = createDetector(CalculationDirection.RightToLeft);

        final double lat = 60;
        final double dlon = LocationUtils.getLongitudeDiff(
                lat, SiblingDetector.MAX_DISTANCE_AVERAGE) / 2.;
        final long min10 = 10 * 60 * 1000l;

        long t = 100 * min10;
        double lon = 10.;

        final List<TrackerEventDto> l1 = new LinkedList<>();
        l1.add(createTrackerEvent(lat, lon += dlon, t+= min10));
        l1.add(createTrackerEvent(lat, lon += dlon, t+= min10));
        l1.add(createTrackerEvent(lat, lon += dlon, t+= min10));
        l1.add(createTrackerEvent(lat, lon += dlon, t+= min10));

        final List<TrackerEventDto> l2 = new LinkedList<>();
        l2.add(createTrackerEvent(lat, lon += dlon, t+= min10));
        l2.add(createTrackerEvent(lat, lon += dlon, t+= min10));
        l2.add(createTrackerEvent(lat, lon += dlon, t+= min10));
        l2.add(createTrackerEvent(lat, lon += dlon, t+= min10));

        assertFalse(SiblingDetectorTest.isSiblings(detector, l2, l1));
        assertFalse(SiblingDetectorTest.isSiblings(detector, l1, l2));
    }
    @Test
    public void testIsTimeIntersectingRightToLeft() {
        final SiblingDetector detector = createDetector(CalculationDirection.RightToLeft);

        final double lat = 60;
        final double dlon = LocationUtils.getLongitudeDiff(
                lat, SiblingDetector.MAX_DISTANCE_AVERAGE) / 5.;
        final double minPath = LocationUtils.getLongitudeDiff(lat, MIN_PATH);
        final long min10 = 10 * 60 * 1000l;

        long t = 100 * min10;
        double lon = 10.;

        final List<TrackerEventDto> l1 = new LinkedList<>();
        final List<TrackerEventDto> l2 = new LinkedList<>();

        l1.add(createTrackerEvent(lat, lon += dlon, t+= min10));
        l1.add(createTrackerEvent(lat, lon += dlon, t+= min10));
        l1.add(createTrackerEvent(lat, lon += dlon, t+= min10));
        l1.add(createTrackerEvent(lat, lon += dlon, t+= min10));

        //intersected time
        final int count = (int) Math.round(minPath / dlon) + 1;
        for (int i = 0; i < count; i++) {
            l1.add(createTrackerEvent(lat, lon += dlon, t+= min10));
            l2.add(createTrackerEvent(lat, lon += dlon, t+= min10));
        }

        //l1 stopped l2 continued
        l2.add(createTrackerEvent(lat, lon += dlon, t+= min10));
        l2.add(createTrackerEvent(lat, lon += dlon, t+= min10));
        l2.add(createTrackerEvent(lat, lon += dlon, t+= min10));
        l2.add(createTrackerEvent(lat, lon += dlon, t+= min10));
        l2.add(createTrackerEvent(lat, lon += dlon, t+= min10));
        l2.add(createTrackerEvent(lat, lon += dlon, t+= min10));
        l2.add(createTrackerEvent(lat, lon += dlon, t+= min10));
        l2.add(createTrackerEvent(lat, lon += dlon, t+= min10));
        l2.add(createTrackerEvent(lat, lon += dlon, t+= min10));
        l2.add(createTrackerEvent(lat, lon += dlon, t+= min10));
        l2.add(createTrackerEvent(lat, lon += dlon, t+= min10));
        l2.add(createTrackerEvent(lat, lon += dlon, t+= min10));

        assertTrue(SiblingDetectorTest.isSiblings(detector, l2, l1));
    }
    /**
     * @param direction
     * @return
     */
    private SiblingDetector createDetector(final CalculationDirection direction) {
        return new SiblingDetector(direction) {
            /* (non-Javadoc)
             * @see com.visfresh.impl.siblingdetect.SiblingDetector#createDetecters(com.visfresh.impl.siblingdetect.CalculationDirection)
             */
            @Override
            protected List<StatefullSiblingDetector> createDetecters(final CalculationDirection d) {
                final List<StatefullSiblingDetector> list = new LinkedList<>();
                list.add(new SiblingDetectorByDistance(direction));
                return list;
            }
        };
    }
    /**
     * @param latitude
     * @param longitude
     * @param time
     * @return
     */
    private TrackerEventDto createTrackerEvent(final double latitude, final double longitude, final long time) {
        final TrackerEventDto e = new TrackerEventDto();
        e.setLatitude(latitude);
        e.setLongitude(longitude);
        e.setTime(new Date(time));
        e.setCreatedOn(e.getTime());
        return e;
    }
}
