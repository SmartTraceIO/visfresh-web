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
public class SiblingDetectorTest {
    /**
     * Default constructor.
     */
    public SiblingDetectorTest() {
        super();
    }

    //LeftToRight
    @Test
    public void testIsSiblingsLeftToRight() {
        final SiblingDetector detector = new SiblingDetector(new LeftToRight());

        //crete master event list
        final List<TrackerEventDto> masterEvents = new LinkedList<>();
        final List<TrackerEventDto> siblingEvents = new LinkedList<>();
        final List<TrackerEventDto> notSiblingEvents = new LinkedList<>();

        final double x0 = 10.;
        final double y0 = 10.;
        final long t0 = System.currentTimeMillis() - 1000000l;
        final long dt = 10 * 60 * 1000l;
        final double minPath = LocationUtils.getLongitudeDiff(y0, SiblingDetector.MIN_PATH);

        //intersected time
        final int count = (int) Math.round(minPath / 0.01) + 1;
        for (int i = 0; i < count; i++) {
            masterEvents.add(createTrackerEvent(x0 + 0.01 * i, y0 + 0.01 * i, t0 + i * dt));
            siblingEvents.add(createTrackerEvent(x0 + 0.01 * i + 0.005,
                    y0 + 0.01 * i + 0.005, t0 + i * dt + 60 * 1000l));
            notSiblingEvents.add(createTrackerEvent(x0 - 0.1 * i - 0.05, y0 - 0.1 * i - 0.05,
                    t0 + dt * i + 60 * 1000l));
        }

        assertTrue(detector.isSiblings(siblingEvents, masterEvents));
        assertFalse(detector.isSiblings(notSiblingEvents, masterEvents));
    }
    @Test
    public void testIsSiblingsByGatewayLeftToRight() {
        final SiblingDetector detector = new SiblingDetector(new LeftToRight());

        //crete master event list
        final List<TrackerEventDto> masterEvents = new LinkedList<>();
        final List<TrackerEventDto> siblingEvents = new LinkedList<>();

        final double x0 = 10.;
        final double y0 = 10.;
        final long t0 = System.currentTimeMillis() - 1000000l;
        final long dt = 10 * 60 * 1000l;
        final double minPath = LocationUtils.getLongitudeDiff(y0, SiblingDetector.MIN_PATH);

        //intersected time
        final int count = (int) Math.round(minPath / 0.01) + 1;
        for (int i = 0; i < count; i++) {
            masterEvents.add(createTrackerEvent(x0 + 0.01 * i, y0 + 0.01 * i, t0 + i * dt));
            //add event which is not sibling by distance
            siblingEvents.add(createTrackerEvent(x0 - 0.1 * i - 0.05, y0 - 0.1 * i - 0.05,
                    t0 + dt * i + 60 * 1000l));
        }

        assertFalse(detector.isSiblings(siblingEvents, masterEvents));

        //add gateway to just one reading to each shipment
        final String gateway = "beacon-gateway";
        masterEvents.get(count / 2).setGateway(gateway);
        siblingEvents.get(count / 3).setGateway(gateway);

        assertTrue(detector.isSiblings(siblingEvents, masterEvents));
    }
    @Test
    public void testExcludeWithSmallPathLeftToRight() {
        final SiblingDetector detector = new SiblingDetector(new LeftToRight());

        //crete master event list
        final List<TrackerEventDto> masterEvents = new LinkedList<>();
        final List<TrackerEventDto> siblingEvents = new LinkedList<>();
        final List<TrackerEventDto> notSiblingEvents = new LinkedList<>();

        final double x0 = 10.;
        final double y0 = 10.;
        final long t0 = System.currentTimeMillis() - 1000000l;
        final long dt = 10 * 60 * 1000l;
        final double minPath = LocationUtils.getLongitudeDiff(y0, SiblingDetector.MIN_PATH / 10);

        //intersected time
        final int count = (int) Math.round(minPath / 0.01);
        for (int i = 0; i < count; i++) {
            masterEvents.add(createTrackerEvent(x0 + 0.01 * i, y0 + 0.01 * i, t0 + i * dt));
            siblingEvents.add(createTrackerEvent(x0 + 0.01 * i + 0.005,
                    y0 + 0.01 * i + 0.005, t0 + i * dt + 60 * 1000l));
            notSiblingEvents.add(createTrackerEvent(x0 - 0.1 * i - 0.05, y0 - 0.1 * i - 0.05,
                    t0 + dt * i + 60 * 1000l));
        }

        assertFalse(detector.isSiblings(siblingEvents, masterEvents));
        assertFalse(detector.isSiblings(notSiblingEvents, masterEvents));
    }
    @Test
    public void testNotIntersectingByTimeLeftToRight() {
        final SiblingDetector detector = new SiblingDetector(new LeftToRight());

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

        assertFalse(detector.isSiblings(e1, e2));
        assertFalse(detector.isSiblings(e2, e1));
    }
    @Test
    public void testIsTimeNotIntersectingLeftToRight() {
        final SiblingDetector detector = new SiblingDetector(new LeftToRight());

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

        assertFalse(detector.isSiblings(l1, l2));
        assertFalse(detector.isSiblings(l2, l1));
    }
    @Test
    public void testIsTimeIntersectingLeftToRight() {
        final SiblingDetector detector = new SiblingDetector(new LeftToRight());

        final double lat = 60;
        final double dlon = LocationUtils.getLongitudeDiff(
                lat, SiblingDetector.MAX_DISTANCE_AVERAGE) / 5.;
        final double minPath = LocationUtils.getLongitudeDiff(lat, SiblingDetector.MIN_PATH);
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

        assertTrue(detector.isSiblings(l1, l2));
    }

    //Right to Left
    @Test
    public void testIsSiblingsRightToLeft() {
        final SiblingDetector detector = new SiblingDetector(new RightToLeft());

        //crete master event list
        final List<TrackerEventDto> masterEvents = new LinkedList<>();
        final List<TrackerEventDto> siblingEvents = new LinkedList<>();
        final List<TrackerEventDto> notSiblingEvents = new LinkedList<>();

        final double x0 = 10.;
        final double y0 = 10.;
        final long t0 = System.currentTimeMillis() - 1000000l;
        final long dt = 10 * 60 * 1000l;
        final double minPath = LocationUtils.getLongitudeDiff(y0, SiblingDetector.MIN_PATH);

        //intersected time
        final int count = (int) Math.round(minPath / 0.01) + 1;
        for (int i = 0; i < count; i++) {
            masterEvents.add(createTrackerEvent(x0 + 0.01 * i, y0 + 0.01 * i, t0 + i * dt));
            siblingEvents.add(createTrackerEvent(x0 + 0.01 * i + 0.005,
                    y0 + 0.01 * i + 0.005, t0 + i * dt + 60 * 1000l));
            notSiblingEvents.add(createTrackerEvent(x0 - 0.1 * i - 0.05, y0 - 0.1 * i - 0.05,
                    t0 + dt * i + 60 * 1000l));
        }

        assertTrue(detector.isSiblings(siblingEvents, masterEvents));
        assertFalse(detector.isSiblings(notSiblingEvents, masterEvents));
    }
    @Test
    public void testIsSiblingsByGatewayRightToLeft() {
        final SiblingDetector detector = new SiblingDetector(new RightToLeft());

        //crete master event list
        final List<TrackerEventDto> masterEvents = new LinkedList<>();
        final List<TrackerEventDto> siblingEvents = new LinkedList<>();

        final double x0 = 10.;
        final double y0 = 10.;
        final long t0 = System.currentTimeMillis() - 1000000l;
        final long dt = 10 * 60 * 1000l;
        final double minPath = LocationUtils.getLongitudeDiff(y0, SiblingDetector.MIN_PATH);

        //intersected time
        final int count = (int) Math.round(minPath / 0.01) + 1;
        for (int i = 0; i < count; i++) {
            masterEvents.add(createTrackerEvent(x0 + 0.01 * i, y0 + 0.01 * i, t0 + i * dt));
            //add event which is not sibling by distance
            siblingEvents.add(createTrackerEvent(x0 - 0.1 * i - 0.05, y0 - 0.1 * i - 0.05,
                    t0 + dt * i + 60 * 1000l));
        }

        assertFalse(detector.isSiblings(siblingEvents, masterEvents));

        //add gateway to just one reading to each shipment
        final String gateway = "beacon-gateway";
        masterEvents.get(count / 2).setGateway(gateway);
        siblingEvents.get(count / 3).setGateway(gateway);

        assertTrue(detector.isSiblings(siblingEvents, masterEvents));
    }
    @Test
    public void testExcludeWithSmallPathRightToLeft() {
        final SiblingDetector detector = new SiblingDetector(new RightToLeft());

        //crete master event list
        final List<TrackerEventDto> masterEvents = new LinkedList<>();
        final List<TrackerEventDto> siblingEvents = new LinkedList<>();
        final List<TrackerEventDto> notSiblingEvents = new LinkedList<>();

        final double x0 = 10.;
        final double y0 = 10.;
        final long t0 = System.currentTimeMillis() - 1000000l;
        final long dt = 10 * 60 * 1000l;
        final double minPath = LocationUtils.getLongitudeDiff(y0, SiblingDetector.MIN_PATH / 10);

        //intersected time
        final int count = (int) Math.round(minPath / 0.01);
        for (int i = 0; i < count; i++) {
            masterEvents.add(createTrackerEvent(x0 + 0.01 * i, y0 + 0.01 * i, t0 + i * dt));
            siblingEvents.add(createTrackerEvent(x0 + 0.01 * i + 0.005,
                    y0 + 0.01 * i + 0.005, t0 + i * dt + 60 * 1000l));
            notSiblingEvents.add(createTrackerEvent(x0 - 0.1 * i - 0.05, y0 - 0.1 * i - 0.05,
                    t0 + dt * i + 60 * 1000l));
        }

        assertFalse(detector.isSiblings(siblingEvents, masterEvents));
        assertFalse(detector.isSiblings(notSiblingEvents, masterEvents));
    }
    @Test
    public void testNotIntersectingByTimeRightToLeft() {
        final SiblingDetector detector = new SiblingDetector(new RightToLeft());

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

        assertFalse(detector.isSiblings(e1, e2));
        assertFalse(detector.isSiblings(e2, e1));
    }
    @Test
    public void testIsTimeNotIntersectingRightToLeft() {
        final SiblingDetector detector = new SiblingDetector(new RightToLeft());

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

        assertFalse(detector.isSiblings(l1, l2));
        assertFalse(detector.isSiblings(l2, l1));
    }
    @Test
    public void testIsTimeIntersectingRightToLeft() {
        final SiblingDetector detector = new SiblingDetector(new RightToLeft());

        final double lat = 60;
        final double dlon = LocationUtils.getLongitudeDiff(
                lat, SiblingDetector.MAX_DISTANCE_AVERAGE) / 5.;
        final double minPath = LocationUtils.getLongitudeDiff(lat, SiblingDetector.MIN_PATH);
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

        assertTrue(detector.isSiblings(l1, l2));
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
