/**
 *
 */
package com.visfresh.impl.siblingdetect;

import java.util.LinkedList;
import java.util.List;

import com.visfresh.io.TrackerEventDto;

public class LeftToRight implements CalculationDirection {
    /* (non-Javadoc)
     * @see com.visfresh.impl.services.SiblingDetectDispatcher.CalculationDirection#isBefore(com.visfresh.io.TrackerEventDto, com.visfresh.io.TrackerEventDto)
     */
    @Override
    public boolean isBefore(final TrackerEventDto e1, final TrackerEventDto e2) {
        return e1.getTime().before(e2.getTime());
    }
    /* (non-Javadoc)
     * @see com.visfresh.impl.siblingdetect.CalculationDirection#createListForProcess(java.util.LinkedList)
     */
    @Override
    public List<TrackerEventDto> createListForProcess(final List<TrackerEventDto> origin) {
        return new LinkedList<>(origin);
    }
}