/**
 *
 */
package com.visfresh.dao;

import java.util.List;
import java.util.Set;

import com.visfresh.entities.Notification;
import com.visfresh.entities.User;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public interface NotificationDao extends
        DaoBase<Notification, Long> {

    /**
     * @param user TODO
     * @return
     */
    List<Notification> findForUser(User user);
    /**
     * @param user the user.
     * @param ids notification IDs.
     */
    void deleteByUserAndId(User user, Set<Long> ids);

}
