/**
 *
 */
package com.visfresh.services;

import java.util.LinkedList;
import java.util.List;

import com.visfresh.entities.ShortTrackerEvent;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class EventsNullCoordinatesCorrector {

    /**
     * Default constructor.
     */
    public EventsNullCoordinatesCorrector() {
        super();
    }

    public void correct(final List<ShortTrackerEvent> events) {
        final List<ShortTrackerEvent> notCorrected = new LinkedList<>();

        ShortTrackerEvent last = null;

        //copy tracker events for avoid of excesses in next usage.
        for (final ShortTrackerEvent e : events) {
            if (isEmpty(e)) {
                if (last != null) {
                    copyCoordinates(last, e);
                } else {
                    notCorrected.add(e);
                }
            } else {
                last = e;
            }

            if (!notCorrected.isEmpty() && last != null) {
                for (final ShortTrackerEvent e1 : notCorrected) {
                    copyCoordinates(last, e1);
                }
                notCorrected.clear();
            }
        }
    }

    /**
     * @param src source event.
     * @param dst target event.
     */
    private void copyCoordinates(final ShortTrackerEvent src, final ShortTrackerEvent dst) {
        dst.setLatitude(src.getLatitude());
        dst.setLongitude(src.getLongitude());
    }

    /**
     * @param e
     * @return
     */
    private boolean isEmpty(final ShortTrackerEvent e) {
        return e.getLatitude() == null || e.getLongitude() == null;
    }
}
