/**
 *
 */
package com.visfresh.dao.mock;

import java.util.LinkedList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.visfresh.dao.NotificationScheduleDao;
import com.visfresh.entities.Company;
import com.visfresh.entities.NotificationSchedule;
import com.visfresh.entities.PersonSchedule;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class MockNotificationScheduleDao extends MockDaoBase<NotificationSchedule, Long> implements NotificationScheduleDao {
    /**
     * Default constructor.
     */
    public MockNotificationScheduleDao() {
        super();
    }

    /* (non-Javadoc)
     * @see com.visfresh.dao.LocationProfileDao#findByCompany(com.visfresh.entities.Company)
     */
    @Override
    public List<NotificationSchedule> findByCompany(final Company company) {
        final List<NotificationSchedule> list = new LinkedList<NotificationSchedule>();
        for (final NotificationSchedule d : entities.values()) {
            if (d.getCompany().getId().equals(company.getId())) {
                list.add(d);
            }
        }
        return list;
    }
    /* (non-Javadoc)
     * @see com.visfresh.dao.mock.MockDaoBase#save(com.visfresh.entities.EntityWithId)
     */
    @Override
    public <S extends NotificationSchedule> S save(final S entity) {
        final S ns = super.save(entity);
        for (final PersonSchedule ps : ns.getSchedules()) {
            if (ps.getId() == null) {
                ps.setId(generateId());
            }
        }
        return ns;
    }
}
