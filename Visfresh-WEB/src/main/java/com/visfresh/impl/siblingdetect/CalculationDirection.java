/**
 *
 */
package com.visfresh.impl.siblingdetect;

import com.visfresh.io.TrackerEventDto;

public abstract class CalculationDirection {
    public static CalculationDirection LeftToRight = new CalculationDirection() {
        /* (non-Javadoc)
         * @see com.visfresh.impl.services.SiblingDetectDispatcher.CalculationDirection#isBefore(com.visfresh.io.TrackerEventDto, com.visfresh.io.TrackerEventDto)
         */
        @Override
        public boolean isBefore(final TrackerEventDto e1, final TrackerEventDto e2) {
            return e1.getTime().before(e2.getTime());
        }
    };

    public static CalculationDirection RightToLeft = new CalculationDirection() {
        /* (non-Javadoc)
         * @see com.visfresh.impl.services.SiblingDetectDispatcher.CalculationDirection#isBefore(com.visfresh.io.TrackerEventDto, com.visfresh.io.TrackerEventDto)
         */
        @Override
        public boolean isBefore(final TrackerEventDto e1, final TrackerEventDto e2) {
            return e2.getTime().before(e1.getTime());
        }
    };
    /**
     * Default constructor.
     */
    private CalculationDirection() {
        super();
    }
    /**
     * @param e1 first event.
     * @param e2 second event.
     * @return true if e1 is before e2
     */
    public abstract boolean isBefore(TrackerEventDto e1, TrackerEventDto e2);
}
