/**
 *
 */
package com.visfresh.impl.siblingdetect;

import java.util.LinkedList;
import java.util.List;

import com.visfresh.impl.siblingdetect.StateFullSiblingDetector.State;
import com.visfresh.io.TrackerEventDto;
import com.visfresh.utils.PushBackIterator;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class SiblingDetector {
    public static final double MAX_DISTANCE_AVERAGE = 3000; //meters

    private final CalculationDirection direction;
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

        final List<StateFullSiblingDetector> detectors = createDetecters(direction);

        final PushBackIterator<TrackerEventDto> iterE1 = new PushBackIterator<>(originE1.iterator());
        final PushBackIterator<TrackerEventDto> iterE2 = new PushBackIterator<>(originE2.iterator());

        while (iterE1.hasNext() || iterE2.hasNext()) {
            State state;
            if (!iterE1.hasNext()) {
                state = runNextCheckSiblings(detectors, null, iterE2.next());
            } else if (!iterE2.hasNext()) {
                state = runNextCheckSiblings(detectors, iterE1.next(), null);
            } else {
                final TrackerEventDto e1 = iterE1.next();
                final TrackerEventDto e2 = iterE2.next();

                if (direction.isBefore(e1, e2)) {
                    state = runNextCheckSiblings(detectors, e1, null);
                    iterE2.pushBack(e2);
                } else {
                    state = runNextCheckSiblings(detectors, null, e2);
                    iterE1.pushBack(e1);
                }
            }

            if (state == State.Siblings) {
                return true;
            } else if (state == State.NotSiblings) {
                return false;
            } //else continue
        }

        //finish detection
        for (final StateFullSiblingDetector d : detectors) {
            d.finish();
        }

        return oneDetectsState(detectors, StateFullSiblingDetector.State.Siblings);
    }
    /**
     * @param detectors list of detectors.
     * @param e1
     * @param e2
     * @return
     */
    private State runNextCheckSiblings(final List<StateFullSiblingDetector> detectors,
            final TrackerEventDto e1, final TrackerEventDto e2) {
        for (final StateFullSiblingDetector d : detectors) {
            final State s = d.next(e1, e2);
            if (s != State.Checking) {
                //first found determined state should stop of processing
                return s;
            }
        }
        return State.Checking;
    }
    /**
     * @param d
     * @return
     */
    protected List<StateFullSiblingDetector> createDetecters(final CalculationDirection d) {
        final List<StateFullSiblingDetector> list = new LinkedList<>();
        list.add(new SiblingDetectorByGateway());
        list.add(new SiblingDetectorByDistance(direction));
        return list;
    }
    /**
     * @param detectors
     * @param state
     * @return
     */
    private boolean oneDetectsState(final List<StateFullSiblingDetector> detectors,
            final State state) {
        for (final StateFullSiblingDetector d : detectors) {
            if (d.getState() == state) {
                return true;
            }
        }
        return false;
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
    /**
     * @return the direction
     */
    public CalculationDirection getDirection() {
        return direction;
    }
}
