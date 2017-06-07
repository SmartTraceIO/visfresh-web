/**
 *
 */
package com.visfresh.dao;

import java.util.List;

import com.visfresh.entities.NotificationSchedule;
import com.visfresh.entities.ReferenceInfo;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public interface NotificationScheduleDao extends EntityWithCompanyDaoBase<NotificationSchedule, NotificationSchedule, Long> {
    /**
     * @param id notification schedule ID.
     * @return list of DB references to schedule with given ID.
     */
    List<ReferenceInfo> getDbReferences(Long id);
}
