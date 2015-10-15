/**
 * 
 */
package com.visfresh.dao;

import org.springframework.data.repository.CrudRepository;

import com.visfresh.entities.NotificationSchedule;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public interface NotificationScheduleDao extends
        CrudRepository<NotificationSchedule, Long> {

}
