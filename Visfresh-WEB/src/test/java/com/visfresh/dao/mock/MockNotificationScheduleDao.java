/**
 *
 */
package com.visfresh.dao.mock;

import org.springframework.stereotype.Component;

import com.visfresh.controllers.NotificationScheduleConstants;
import com.visfresh.dao.NotificationScheduleDao;
import com.visfresh.entities.NotificationSchedule;
import com.visfresh.entities.PersonSchedule;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class MockNotificationScheduleDao extends MockEntityWithCompanyDaoBase<NotificationSchedule, Long> implements NotificationScheduleDao {
    /**
     * Default constructor.
     */
    public MockNotificationScheduleDao() {
        super();
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
    /* (non-Javadoc)
     * @see com.visfresh.dao.mock.MockDaoBase#getValueForFilterOrCompare(java.lang.String, com.visfresh.entities.EntityWithId)
     */
    @Override
    protected Object getValueForFilterOrCompare(final String property,
            final NotificationSchedule t) {
        if (property.equals(NotificationScheduleConstants.PROPERTY_NOTIFICATION_SCHEDULE_NAME)) {
            return t.getName();
        }
        if (property.equals(NotificationScheduleConstants.PROPERTY_NOTIFICATION_SCHEDULE_ID)) {
            return t.getId();
        }
        if (property.equals(NotificationScheduleConstants.PROPERTY_NOTIFICATION_SCHEDULE_DESCRIPTION)) {
            return t.getDescription();
        }
        throw new IllegalArgumentException("Unsupported property: " + property);
    }
}
