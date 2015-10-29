/**
 *
 */
package com.visfresh.dao.mock;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.visfresh.dao.TrackerEventDao;
import com.visfresh.entities.Shipment;
import com.visfresh.entities.TrackerEvent;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class MockTrackerEventDao extends MockDaoBase<TrackerEvent, Long> implements TrackerEventDao {
    /**
     * Default constructor.
     */
    public MockTrackerEventDao() {
        super();
    }

    /* (non-Javadoc)
     * @see com.visfresh.dao.TrackerEventDao#getFirstHotOccurence(com.visfresh.entities.TrackerEvent, double)
     */
    @Override
    public Date getFirstHotOccurence(final TrackerEvent e, final double minimalTemperature) {
        final ArrayList<TrackerEvent> list = new ArrayList<TrackerEvent>(entities.values());
        orderById(list, true);

        final int index = list.indexOf(e);
        TrackerEvent prev = null;
        for (int i = index - 1; i >= 0; i--) {
            final TrackerEvent t = list.get(i);
            if (t.getTemperature() > minimalTemperature) {
                return prev == null ? null : t.getTime();
            }
            prev = t;
        }

        return null;
    }

    /* (non-Javadoc)
     * @see com.visfresh.dao.TrackerEventDao#getFirstColdOccurence(com.visfresh.entities.TrackerEvent, double)
     */
    @Override
    public Date getFirstColdOccurence(final TrackerEvent e, final double maximalTemperature) {
        final ArrayList<TrackerEvent> list = new ArrayList<TrackerEvent>(entities.values());
        orderById(list, true);

        final int index = list.indexOf(e);
        TrackerEvent prev = null;
        for (int i = index - 1; i >= 0; i--) {
            final TrackerEvent t = list.get(i);
            if (t.getTemperature() < maximalTemperature) {
                return prev == null ? null : t.getTime();
            }
            prev = t;
        }

        return null;
    }

    /* (non-Javadoc)
     * @see com.visfresh.dao.TrackerEventDao#getEvents(com.visfresh.entities.Shipment, java.util.Date, java.util.Date)
     */
    @Override
    public List<TrackerEvent> getEvents(final Shipment shipment, final Date fromDate,
            final Date toDate) {
        final List<TrackerEvent> events = new LinkedList<TrackerEvent>();
        for (final TrackerEvent e : entities.values()) {
            final Date date = e.getTime();
            if (!date.before(fromDate) && !date.after(toDate)) {
                events.add(e);
            }
        }
        return orderById(events, true);
    }
}
