/**
 *
 */
package com.visfresh.services;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.visfresh.entities.ShortTrackerEvent;
import com.visfresh.utils.LocationUtils;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
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

        final List<ShortTrackerEvent> toOptimize = new LinkedList<>();

        //copy tracker events for avoid of excesses in next usage.
        for (final ShortTrackerEvent eOrigin : origin) {
            final ShortTrackerEvent e = new ShortTrackerEvent(eOrigin);

            switch (eOrigin.getType()) {
                case STP:
                    if (!toOptimize.isEmpty()) {
                        move(toOptimize, readings);
                    } else {
                        toOptimize.add(e);
                    }
                    break;
                case VIB:
                    if (!toOptimize.isEmpty()) {
                        normalize(toOptimize);
                        move(toOptimize, readings);
                    }
                    break;
                default:
                    if (toOptimize.isEmpty()) {
                        readings.add(e);
                    } else {
                        toOptimize.add(e);
                    }
            }
        }

        if (!toOptimize.isEmpty()) {
            normalize(toOptimize);
            move(toOptimize, readings);
        }

        return readings;
    }

    /**
     * @param events
     */
    private void normalize(final List<ShortTrackerEvent> events) {
        final List<ShortTrackerEvent> tmp = new LinkedList<>();
        ShortTrackerEvent first = null;

        for (final ShortTrackerEvent e : events) {
            if (tmp.size() > 5 || (first != null && !isNearFirst(first, e))) {
                correctCoordinates(tmp);
                tmp.clear();
                first = null;
            }

            tmp.add(e);
            if (tmp.size() == 1) {
                first = e;
            }
        }

        if (tmp.size() > 0) {
            correctCoordinates(tmp);
        }
    }

    /**
     * @param events
     */
    private void correctCoordinates(final List<ShortTrackerEvent> events) {
        final Iterator<ShortTrackerEvent> iter = events.iterator();

        final ShortTrackerEvent first = iter.next();
        while (iter.hasNext()) {
            final ShortTrackerEvent e = iter.next();
            e.setLatitude(first.getLatitude());
            e.setLongitude(first.getLongitude());
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
                e1.getLatitude(), e1.getLongitude()) <= 2000;
    }
    /**
     * @param src
     * @param dst
     */
    private void move(final List<ShortTrackerEvent> src, final List<ShortTrackerEvent> dst) {
        dst.addAll(src);
        src.clear();
    }
}
