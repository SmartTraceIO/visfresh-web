/**
 *
 */
package com.visfresh.dao.mock;

import java.util.LinkedList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.visfresh.dao.ShipmentTemplateDao;
import com.visfresh.entities.Company;
import com.visfresh.entities.NotificationSchedule;
import com.visfresh.entities.ShipmentTemplate;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class MockShipmentTemplateDao extends MockDaoBase<ShipmentTemplate, Long> implements ShipmentTemplateDao {
    /**
     * Default constructor.
     */
    public MockShipmentTemplateDao() {
        super();
    }

    /* (non-Javadoc)
     * @see com.visfresh.dao.mock.MockDaoBase#save(com.visfresh.entities.EntityWithId)
     */
    @Override
    public <S extends ShipmentTemplate> S save(final S entity) {
        final S e = super.save(entity);
        for (final NotificationSchedule n : e.getAlertsNotificationSchedules()) {
            if (n.getId() == null) {
                n.setId(generateId());
            }
        }
        return e;
    }
    /* (non-Javadoc)
     * @see com.visfresh.dao.ShipmentDao#findByCompany(com.visfresh.entities.Company)
     */
    @Override
    public List<ShipmentTemplate> findByCompany(final Company company) {
        final List<ShipmentTemplate> list = new LinkedList<ShipmentTemplate>();
        for (final ShipmentTemplate s : findAll()) {
            if (s.getCompany().getId().equals(company.getId())) {
                list.add(s);
            }
        }
        return list;
    }
}
