/**
 *
 */
package com.visfresh.dao.mock;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.springframework.stereotype.Component;

import com.visfresh.dao.NotificationDao;
import com.visfresh.entities.Notification;
import com.visfresh.entities.User;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class MockNotificationDao extends MockDaoBase<Notification, Long> implements NotificationDao {
    /**
     * Default constructor.
     */
    public MockNotificationDao() {
        super();
    }

    /* (non-Javadoc)
     * @see com.visfresh.dao.NotificationDao#findForUser(com.visfresh.entities.User)
     */
    @Override
    public List<Notification> findForUser(final User user) {
        final List<Notification> list = new LinkedList<Notification>();
        for (final Notification n : entities.values()) {
            if (n.getUser().getId().equals(user.getId())) {
                list.add(n);
            }
        }
        return list;
    }

    /* (non-Javadoc)
     * @see com.visfresh.dao.NotificationDao#deleteByUserAndId(com.visfresh.entities.User, java.util.Set)
     */
    @Override
    public void deleteByUserAndId(final User user, final Set<Long> ids) {
        final Iterator<Map.Entry<Long, Notification>> iter = entities.entrySet().iterator();
        while (iter.hasNext()) {
            final Entry<Long, Notification> next = iter.next();
            if (ids.contains(next.getKey()) && next.getValue().getUser().getId().equals(user.getId())) {
                iter.remove();
            }
        }
    }
}
