/**
 *
 */
package com.visfresh.reports.geomap;

import java.util.LinkedList;
import java.util.List;

import com.visfresh.entities.ShortTrackerEvent;
import com.visfresh.utils.LocationUtils;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class EventsOptimizer {
    /**
     * Default constructor.
     */
    public EventsOptimizer() {
        super();
    }

    public List<ShortTrackerEvent> optimize(final List<ShortTrackerEvent> origin) {
        final List<ShortTrackerEvent> readings = new LinkedList<>();

        //do optimize
        //end result is lat long
        //[15:58:38] James Richardson: we might limit the dampening more
        //[15:58:45] James Richardson: only for 5 readings max
        //[15:58:51] James Richardson: only if moves les than 1km

        final List<ShortTrackerEvent> quarantine = new LinkedList<>();

        //copy tracker events for avoid of excesses in next usage.
        for (final ShortTrackerEvent eOrigin : origin) {
            final ShortTrackerEvent e = new ShortTrackerEvent(eOrigin);

            switch (eOrigin.getType()) {
                case STP:
                    if (!quarantine.isEmpty()) {
                        flushQuarantine(readings, quarantine);
                    } else {
                        quarantine.add(e);
                    }
                    break;
                case VIB:
                    if (!quarantine.isEmpty()) {
                        normalize(quarantine);
                        flushQuarantine(readings, quarantine);
                    }
                    break;
                default:
                    if (quarantine.isEmpty()) {
                        readings.add(e);
                    } else if (isNearFirst(quarantine.get(0), e)) {
                        quarantine.add(e);
                    } else {
                        flushQuarantine(readings, quarantine);
                    }
            }
        }

        if (!quarantine.isEmpty()) {
            normalize(quarantine);
            flushQuarantine(readings, quarantine);
        }

        return readings;
    }

    /**
     * @param events
     */
    private void normalize(final List<ShortTrackerEvent> events) {
        final List<ShortTrackerEvent> tmp = new LinkedList<>();

        for (final ShortTrackerEvent e : events) {
            tmp.add(e);
            if (tmp.size() > 9) {
                averageCoordinates(tmp);
                tmp.clear();
            }
        }

        if (tmp.size() > 0) {
            averageCoordinates(tmp);
        }
    }

    /**
     * @param events calculates and set average coordinates to all events.
     */
    private void averageCoordinates(final List<ShortTrackerEvent> events) {
        double lat = 0;
        double lon = 0;
        for (final ShortTrackerEvent e : events) {
            lat += e.getLatitude();
            lon += e.getLongitude();
        }

        lat /= events.size();
        lon /= events.size();

        for (final ShortTrackerEvent e : events) {
            e.setLatitude(lat);
            e.setLongitude(lon);
        }
    }

    /**
     * @param e1
     * @param e2
     * @return
     */
    private boolean isNearFirst(final ShortTrackerEvent e1,
            final ShortTrackerEvent e2) {
        return LocationUtils.getDistanceMeters(e2.getLatitude(), e2.getLongitude(),
                e1.getLatitude(), e1.getLongitude()) <= 1000;
    }

    /**
     * @param readings
     * @param quarantine
     */
    private void flushQuarantine(final List<ShortTrackerEvent> readings,
            final List<ShortTrackerEvent> quarantine) {
        readings.addAll(quarantine);
        quarantine.clear();
    }
}
