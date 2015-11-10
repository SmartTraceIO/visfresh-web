/**
 *
 */
package com.visfresh.dao.mock;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.visfresh.dao.ArrivalDao;
import com.visfresh.entities.Arrival;
import com.visfresh.entities.Shipment;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class MockArrivalDao extends MockDaoBase<Arrival, Long> implements ArrivalDao {
    /**
     * Default constructor.
     */
    public MockArrivalDao() {
        super();
    }

    /* (non-Javadoc)
     * @see com.visfresh.dao.AlertDao#getAlerts(com.visfresh.entities.Shipment, java.util.Date, java.util.Date)
     */
    @Override
    public List<Arrival> getArrivals(final Shipment shipment, final Date fromDate, final Date toDate) {
        final List<Arrival> alerts = new LinkedList<Arrival>();
        for (final Arrival a : entities.values()) {
            final Date date = a.getDate();
            if (!date.before(fromDate) && !date.after(toDate)) {
                alerts.add(a);
            }
        }
        return orderById(alerts, true);
    }
    /* (non-Javadoc)
     * @see com.visfresh.dao.mock.MockDaoBase#getValueForFilterOrCompare(java.lang.String, com.visfresh.entities.EntityWithId)
     */
    @Override
    protected Object getValueForFilterOrCompare(final String property, final Arrival t) {
        return null;
    }
}
