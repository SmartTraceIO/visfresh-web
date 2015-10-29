/**
 *
 */
package com.visfresh.dao.mock;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.visfresh.dao.AlertDao;
import com.visfresh.entities.Alert;
import com.visfresh.entities.Shipment;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class MockAlertDao extends MockDaoBase<Alert, Long> implements AlertDao {
    /**
     * Default constructor.
     */
    public MockAlertDao() {
        super();
    }

    /* (non-Javadoc)
     * @see com.visfresh.dao.AlertDao#getAlerts(com.visfresh.entities.Shipment, java.util.Date, java.util.Date)
     */
    @Override
    public List<Alert> getAlerts(final Shipment shipment, final Date fromDate, final Date toDate) {
        final List<Alert> alerts = new LinkedList<Alert>();
        for (final Alert a : entities.values()) {
            final Date date = a.getDate();
            if (!date.before(fromDate) && !date.after(toDate)) {
                alerts.add(a);
            }
        }
        return orderById(alerts, true);
    }
}
