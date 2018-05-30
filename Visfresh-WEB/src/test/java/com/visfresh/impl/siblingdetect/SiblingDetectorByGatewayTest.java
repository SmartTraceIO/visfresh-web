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
public class SiblingDetectorByGatewayTest {
    private static final double MIN_PATH = 5000.; //meters

    /**
     * Default constructor.
     */
    public SiblingDetectorByGatewayTest() {
        super();
    }
    @Test
    public void testIsSiblingsByGatewayLeftToRight() {
        final SiblingDetector detector = createDetector(CalculationDirection.LeftToRight);

        //crete master event list
        final List<TrackerEventDto> masterEvents = new LinkedList<>();
        final List<TrackerEventDto> siblingEvents = new LinkedList<>();

        final double x0 = 10.;
        final double y0 = 10.;
        final long t0 = System.currentTimeMillis() - 1000000l;
        final long dt = 10 * 60 * 1000l;
        final double minPath = LocationUtils.getLongitudeDiff(y0, MIN_PATH);

        //intersected time
        final int count = (int) Math.round(minPath / 0.01) + 1;
        for (int i = 0; i < count; i++) {
            masterEvents.add(createTrackerEvent(x0 + 0.01 * i, y0 + 0.01 * i, t0 + i * dt));
            //add event which is not sibling by distance
            siblingEvents.add(createTrackerEvent(x0 - 0.1 * i - 0.05, y0 - 0.1 * i - 0.05,
                    t0 + dt * i + 60 * 1000l));
        }

        assertFalse(SiblingDetectorTest.isSiblings(detector, siblingEvents, masterEvents));

        //add gateway to just one reading to each shipment
        final String gateway = "beacon-gateway";
        masterEvents.get(count / 2).setGateway(gateway);
        siblingEvents.get(count / 3).setGateway(gateway);

        assertTrue(SiblingDetectorTest.isSiblings(detector, siblingEvents, masterEvents));
    }
    @Test
    public void testIsSiblingsByGatewayRightToLeft() {
        final SiblingDetector detector = createDetector(CalculationDirection.RightToLeft);

        //crete master event list
        final List<TrackerEventDto> masterEvents = new LinkedList<>();
        final List<TrackerEventDto> siblingEvents = new LinkedList<>();

        final double x0 = 10.;
        final double y0 = 10.;
        final long t0 = System.currentTimeMillis() - 1000000l;
        final long dt = 10 * 60 * 1000l;
        final double minPath = LocationUtils.getLongitudeDiff(y0, MIN_PATH);

        //intersected time
        final int count = (int) Math.round(minPath / 0.01) + 1;
        for (int i = 0; i < count; i++) {
            masterEvents.add(createTrackerEvent(x0 + 0.01 * i, y0 + 0.01 * i, t0 + i * dt));
            //add event which is not sibling by distance
            siblingEvents.add(createTrackerEvent(x0 - 0.1 * i - 0.05, y0 - 0.1 * i - 0.05,
                    t0 + dt * i + 60 * 1000l));
        }

        assertFalse(SiblingDetectorTest.isSiblings(detector, siblingEvents, masterEvents));

        //add gateway to just one reading to each shipment
        final String gateway = "beacon-gateway";
        masterEvents.get(count / 2).setGateway(gateway);
        siblingEvents.get(count / 3).setGateway(gateway);

        assertTrue(SiblingDetectorTest.isSiblings(detector, siblingEvents, masterEvents));
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
            protected final List<StatefullSiblingDetector> createDetecters(final CalculationDirection d) {
                final List<StatefullSiblingDetector> list = new LinkedList<>();
                list.add(new SiblingDetectorByGateway());
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
