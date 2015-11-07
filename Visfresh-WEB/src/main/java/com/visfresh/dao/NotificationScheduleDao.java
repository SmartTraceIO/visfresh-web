/**
 *
 */
package com.visfresh.dao;

import java.util.List;

import com.visfresh.entities.Company;
import com.visfresh.entities.NotificationSchedule;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public interface NotificationScheduleDao extends
        DaoBase<NotificationSchedule, Long> {
    /**
     * @param company company.
     * @return list of notification schedules.
     */
    List<NotificationSchedule> findByCompany(Company company);
}
