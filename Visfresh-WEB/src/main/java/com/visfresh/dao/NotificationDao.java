/**
 *
 */
package com.visfresh.dao;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import com.visfresh.entities.Notification;
import com.visfresh.entities.NotificationType;
import com.visfresh.entities.User;
import com.visfresh.entities.AppUserNotification;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public interface NotificationDao extends
        DaoBase<Notification, Notification, Long> {

    /**
     * @param user user
     * @param excludeLight TODO
     * @param sorting sorting.
     * @param filter filtering
     * @param page page.
     * @return
     */
    List<AppUserNotification> findForUser(User user, boolean excludeLight, Sorting sorting, Filter filter, Page page);
    /**
     * @param user the user.
     * @param ids notification IDs.
     */
    void markAsReadenByUserAndId(User user, Set<Long> ids);
    /**
     * @param user user.
     * @param excludeLight TODO
     * @param filter filter.
     * @return count of user satisfying given filter.
     */
    int getEntityCount(User user, boolean excludeLight, Filter filter);
    /**
     * @param ids collection of ID.
     * @param type issue type.
     * @return list of notifications for given ID collection.
     */
    List<Notification> getForIssues(Collection<Long> ids, NotificationType type);
}
