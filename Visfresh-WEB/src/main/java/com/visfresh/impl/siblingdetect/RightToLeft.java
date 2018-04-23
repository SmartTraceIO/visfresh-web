/**
 *
 */
package com.visfresh.impl.siblingdetect;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import com.visfresh.io.TrackerEventDto;

public class RightToLeft implements CalculationDirection {
    /* (non-Javadoc)
     * @see com.visfresh.impl.services.SiblingDetectDispatcher.CalculationDirection#isBefore(com.visfresh.io.TrackerEventDto, com.visfresh.io.TrackerEventDto)
     */
    @Override
    public boolean isBefore(final TrackerEventDto e1, final TrackerEventDto e2) {
        return e2.getTime().before(e1.getTime());
    }
    /* (non-Javadoc)
     * @see com.visfresh.impl.siblingdetect.CalculationDirection#createListForProcess(java.util.LinkedList)
     */
    @Override
    public List<TrackerEventDto> createListForProcess(final List<TrackerEventDto> origin) {
        final List<TrackerEventDto> reverted = new LinkedList<>(origin);
        Collections.reverse(reverted);
        return reverted;
    }
}
