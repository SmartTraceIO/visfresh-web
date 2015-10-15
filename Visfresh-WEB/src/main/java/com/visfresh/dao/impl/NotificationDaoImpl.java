/**
 *
 */
package com.visfresh.dao.impl;

import java.util.LinkedList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.visfresh.dao.NotificationDao;
import com.visfresh.entities.Notification;
import com.visfresh.entities.User;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class NotificationDaoImpl extends DaoImplBase<Notification, Long> implements NotificationDao {
    /**
     * Default constructor.
     */
    public NotificationDaoImpl() {
        super(Notification.class);
    }
    /* (non-Javadoc)
     * @see com.visfresh.dao.NotificationDao#findByShipment(java.lang.Long)
     */
    @Override
    public List<Notification> findByShipment(final Long shipment) {
        //TODO implement
        return new LinkedList<Notification>();
    }
    /* (non-Javadoc)
     * @see com.visfresh.dao.NotificationDao#deleteByUserAndId(com.visfresh.entities.User, java.util.List)
     */
    @Override
    public void deleteByUserAndId(final User user, final List<Long> ids) {
        // TODO Auto-generated method stub
    }
}
