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

import com.visfresh.controllers.NotificationConstants;
import com.visfresh.dao.Filter;
import com.visfresh.dao.NotificationDao;
import com.visfresh.dao.Page;
import com.visfresh.dao.Sorting;
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
    public List<Notification> findForUser(final User user, final Sorting sorting, final Filter filter, final Page page) {
        final List<Notification> list = getAllByUser(user, sorting, filter);
        return getPage(list, page);
    }

    /**
     * @param user
     * @param sorting
     * @param filter
     * @return
     */
    protected List<Notification> getAllByUser(final User user, final Sorting sorting,
            final Filter filter) {
        final List<Notification> list = new LinkedList<Notification>();
        for (final Notification n : findAll(filter, sorting, null)) {
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
    /* (non-Javadoc)
     * @see com.visfresh.dao.NotificationDao#getEntityCount(com.visfresh.entities.User, com.visfresh.dao.Filter)
     */
    @Override
    public int getEntityCount(final User user, final Filter filter) {
        return getAllByUser(user, null, filter).size();
    }
    /* (non-Javadoc)
     * @see com.visfresh.dao.mock.MockDaoBase#getValueForFilterOrCompare(java.lang.String, com.visfresh.entities.EntityWithId)
     */
    @Override
    protected Object getValueForFilterOrCompare(final String property, final Notification t) {
        if (property.equals(NotificationConstants.PROPERTY_ID)) {
            return t.getId();
        }
        if (property.equals(NotificationConstants.PROPERTY_TYPE)) {
            return t.getType();
        }
        throw new IllegalArgumentException("Undefined property: " + property);
    }
}
