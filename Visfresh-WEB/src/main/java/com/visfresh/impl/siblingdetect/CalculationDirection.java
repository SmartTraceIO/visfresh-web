/**
 *
 */
package com.visfresh.impl.siblingdetect;

import java.util.List;

import com.visfresh.io.TrackerEventDto;

public interface CalculationDirection {
    /**
     * @param e1 first event.
     * @param e2 second event.
     * @return true if e1 is before e2
     */
    boolean isBefore(TrackerEventDto e1, TrackerEventDto e2);
    /**
     * @param origin
     * @return event list for process.
     */
    List<TrackerEventDto> createListForProcess(List<TrackerEventDto> origin);
}