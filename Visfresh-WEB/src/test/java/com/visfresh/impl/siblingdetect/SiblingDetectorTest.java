/**
 *
 */
package com.visfresh.impl.siblingdetect;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;

import com.visfresh.impl.siblingdetect.StateFullSiblingDetector.State;
import com.visfresh.io.TrackerEventDto;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class SiblingDetectorTest {
    final List<StateFullSiblingDetector> detectors = new LinkedList<>();
    private long id = 1;

    /**
     * Default constructor.
     */
    public SiblingDetectorTest() {
        super();
    }

    @Test
    public void testStopsImmediatellyWhenReturnsSiblingsState() {
        final AtomicInteger numInvokes = new AtomicInteger(0);
        final List<State> states = new LinkedList<>();
        states.add(State.Siblings);

        detectors.add(createInvokesCounter(numInvokes, states));
        final SiblingDetector d = createDetector(CalculationDirection.LeftToRight);

        long startTime = System.currentTimeMillis() - 100000000l;
        final List<TrackerEventDto> e1 = new LinkedList<>();
        e1.add(createEvent(startTime += 10000));
        e1.add(createEvent(startTime += 10000));

        final List<TrackerEventDto> e2 = new LinkedList<>();
        e2.add(createEvent(startTime += 10000));
        e2.add(createEvent(startTime += 10000));

        assertTrue(isSiblings(d, e1, e2));
        assertEquals(1, numInvokes.get());
    }
    @Test
    public void testStopsImmediatellyWhenReturnsNotSiblingsState() {
        final AtomicInteger numInvokes = new AtomicInteger(0);
        final List<State> states = new LinkedList<>();
        states.add(State.Siblings);

        detectors.add(createInvokesCounter(numInvokes, states));

        final SiblingDetector d = createDetector(CalculationDirection.LeftToRight);

        long startTime = System.currentTimeMillis() - 100000000l;
        final List<TrackerEventDto> e1 = new LinkedList<>();
        e1.add(createEvent(startTime += 10000));
        e1.add(createEvent(startTime += 10000));

        final List<TrackerEventDto> e2 = new LinkedList<>();
        e2.add(createEvent(startTime += 10000));
        e2.add(createEvent(startTime += 10000));

        assertTrue(isSiblings(d, e1, e2));
        assertEquals(1, numInvokes.get());
    }
    @Test
    public void testReminder() {
        final AtomicInteger numInvokes = new AtomicInteger(0);
        detectors.add(createInvokesCounter(numInvokes));

        long startTime = System.currentTimeMillis() - 100000000l;
        final List<TrackerEventDto> e1 = new LinkedList<>();
        e1.add(createEvent(startTime += 10000));
        e1.add(createEvent(startTime += 10000));

        final List<TrackerEventDto> e2 = new LinkedList<>();
        e2.add(createEvent(startTime += 10000));

        SiblingDetector d = createDetector(CalculationDirection.LeftToRight);
        assertFalse(isSiblings(d, e1, e2));
        assertEquals(3, numInvokes.get());

        numInvokes.set(0);
        detectors.set(0, createInvokesCounter(numInvokes));
        d = createDetector(CalculationDirection.LeftToRight);
        assertFalse(isSiblings(d, e2, e1));
        assertEquals(3, numInvokes.get());
    }
    @Test
    public void testOrder() {
        final List<TrackerEventDto> events = new LinkedList<>();

        final StateFullSiblingDetector detector = createEventsCatch(events);
        detectors.add(detector);

        long startTime = System.currentTimeMillis() - 100000000l;

        final List<TrackerEventDto> l1 = new LinkedList<>();
        final TrackerEventDto e1 = createEvent(startTime += 10000);
        l1.add(e1);
        final TrackerEventDto e2 = createEvent(startTime += 10000);
        l1.add(e2);

        final List<TrackerEventDto> l2 = new LinkedList<>();
        final TrackerEventDto e3 = createEvent(startTime += 10000);
        l2.add(e3);
        final TrackerEventDto e4 = createEvent(startTime += 10000);
        l2.add(e4);

        SiblingDetector d = createDetector(CalculationDirection.LeftToRight);
        assertFalse(isSiblings(d, l1, l2));
        //test order
        assertEquals(e1.getId(), events.get(0).getId());
        assertEquals(e2.getId(), events.get(1).getId());
        assertEquals(e3.getId(), events.get(2).getId());
        assertEquals(e4.getId(), events.get(3).getId());

        detectors.set(0, createEventsCatch(events));
        d = createDetector(CalculationDirection.LeftToRight);
        events.clear();
        assertFalse(isSiblings(d, l2, l1));
        //test order
        assertEquals(e1.getId(), events.get(0).getId());
        assertEquals(e2.getId(), events.get(1).getId());
        assertEquals(e3.getId(), events.get(2).getId());
        assertEquals(e4.getId(), events.get(3).getId());
    }
    @Test
    public void testMixedOrder() {
        final List<TrackerEventDto> events = new LinkedList<>();
        detectors.add(createEventsCatch(events));

        long startTime = System.currentTimeMillis() - 100000000l;

        final List<TrackerEventDto> l1 = new LinkedList<>();
        final List<TrackerEventDto> l2 = new LinkedList<>();

        final TrackerEventDto e1 = createEvent(startTime += 10000);
        l1.add(e1);
        final TrackerEventDto e2 = createEvent(startTime += 10000);
        l2.add(e2);

        final TrackerEventDto e3 = createEvent(startTime += 10000);
        l1.add(e3);
        final TrackerEventDto e4 = createEvent(startTime += 10000);
        l2.add(e4);

        SiblingDetector d = createDetector(CalculationDirection.LeftToRight);
        assertFalse(isSiblings(d, l1, l2));
        //test order
        assertEquals(e1.getId(), events.get(0).getId());
        assertEquals(e2.getId(), events.get(1).getId());
        assertEquals(e3.getId(), events.get(2).getId());
        assertEquals(e4.getId(), events.get(3).getId());

        detectors.set(0, createEventsCatch(events));
        d = createDetector(CalculationDirection.LeftToRight);
        events.clear();
        assertFalse(isSiblings(d, l2, l1));
        //test order
        assertEquals(e1.getId(), events.get(0).getId());
        assertEquals(e2.getId(), events.get(1).getId());
        assertEquals(e3.getId(), events.get(2).getId());
        assertEquals(e4.getId(), events.get(3).getId());
    }
    /**
     * @param detector
     * @param e1Origin
     * @param e2Origin
     * @return
     */
    private boolean isSiblings(final SiblingDetector detector, final List<TrackerEventDto> e1Origin,
            final List<TrackerEventDto> e2Origin) {
        final List<TrackerEventDto> e1 = new LinkedList<>(e1Origin);
        final List<TrackerEventDto> e2 = new LinkedList<>(e2Origin);

        if (detector.getDirection() == CalculationDirection.RightToLeft) {
            Collections.reverse(e1);
            Collections.reverse(e2);
        }
        return detector.isSiblings(e1, e2);
    }
    /**
     * @param numInvokes
     * @return
     */
    private StateFullSiblingDetector createInvokesCounter(final AtomicInteger numInvokes) {
        return createInvokesCounter(numInvokes, new LinkedList<>());
    }
    /**
     * @param numInvokes
     * @param states
     * @return
     */
    protected StateFullSiblingDetector createInvokesCounter(final AtomicInteger numInvokes, final List<State> states) {
        return new StateFullSiblingDetector() {
            @Override
            protected void doNext(final TrackerEventDto e1, final TrackerEventDto e2) {
                numInvokes.addAndGet(1);
                if (states.size() > 0) {
                    setState(states.remove(0));
                } else {
                    setState(State.Checking);
                }
            }
        };
    }
    /**
     * @param events
     * @return
     */
    protected StateFullSiblingDetector createEventsCatch(final List<TrackerEventDto> events) {
        return new StateFullSiblingDetector() {
            @Override
            protected void doNext(final TrackerEventDto e1, final TrackerEventDto e2) {
                if (e1 != null) {
                    events.add(e1);
                }
                if (e2 != null) {
                    events.add(e2);
                }
            }
        };
    }
    /**
     * @param time
     * @return
     */
    private TrackerEventDto createEvent(final long time) {
        return createEvent(id++, time);
    }
    /**
     * @param time
     * @return
     */
    private TrackerEventDto createEvent(final long id, final long time) {
        final TrackerEventDto e = new TrackerEventDto();
        e.setTime(new Date(time));
        e.setCreatedOn(e.getTime());
        return e;
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
            protected final List<StateFullSiblingDetector> createDetecters(final CalculationDirection d) {
                return detectors;
            }
        };
    }
}
