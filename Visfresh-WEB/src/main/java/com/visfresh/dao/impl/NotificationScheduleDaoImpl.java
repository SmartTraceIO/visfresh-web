/**
 *
 */
package com.visfresh.dao.impl;

import org.springframework.stereotype.Component;

import com.visfresh.dao.NotificationScheduleDao;
import com.visfresh.entities.NotificationSchedule;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class NotificationScheduleDaoImpl extends DaoImplBase<NotificationSchedule, Long>
    implements NotificationScheduleDao {
    /**
     * Default constructor.
     */
    public NotificationScheduleDaoImpl() {
        super(NotificationSchedule.class);
    }
}
