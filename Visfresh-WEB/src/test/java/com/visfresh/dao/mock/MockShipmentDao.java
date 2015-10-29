/**
 *
 */
package com.visfresh.dao.mock;

import java.util.LinkedList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.visfresh.dao.ShipmentDao;
import com.visfresh.entities.Company;
import com.visfresh.entities.NotificationSchedule;
import com.visfresh.entities.Shipment;
import com.visfresh.entities.ShipmentStatus;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class MockShipmentDao extends MockDaoBase<Shipment, Long> implements ShipmentDao {
    /**
     * Default constructor.
     */
    public MockShipmentDao() {
        super();
    }

    /* (non-Javadoc)
     * @see com.visfresh.dao.mock.MockDaoBase#save(com.visfresh.entities.EntityWithId)
     */
    @Override
    public <S extends Shipment> S save(final S entity) {
        final S e = super.save(entity);
        for (final NotificationSchedule n : e.getAlertsNotificationSchedules()) {
            if (n.getId() == null) {
                n.setId(generateId());
            }
        }
        return e;
    }
    /* (non-Javadoc)
     * @see com.visfresh.dao.ShipmentDao#findActiveShipment(java.lang.String)
     */
    @Override
    public Shipment findActiveShipment(final String imei) {
        for (final Shipment s : entities.values()) {
            if (s.getDevice().getImei().equals(imei) && s.getStatus() != ShipmentStatus.Complete) {
                return s;
            }
        }
        return null;
    }

    /* (non-Javadoc)
     * @see com.visfresh.dao.ShipmentDao#findByCompany(com.visfresh.entities.Company)
     */
    @Override
    public List<Shipment> findByCompany(final Company company) {
        final List<Shipment> list = new LinkedList<Shipment>();
        for (final Shipment s : entities.values()) {
            if (s.getCompany().getId().equals(company.getId())) {
                list.add(s);
            }
        }
        return list;
    }
}
