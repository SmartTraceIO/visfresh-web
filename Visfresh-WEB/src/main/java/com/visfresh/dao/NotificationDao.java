/**
 *
 */
package com.visfresh.dao;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

import com.visfresh.entities.Notification;
import com.visfresh.entities.User;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public interface NotificationDao extends
        CrudRepository<Notification, Long> {

    /**
     * @param shipment
     * @return
     */
    List<Notification> findByShipment(Long shipment);
    /**
     * @param user the user.
     * @param ids notification IDs.
     */
    void deleteByUserAndId(User user, List<Long> ids);

}
