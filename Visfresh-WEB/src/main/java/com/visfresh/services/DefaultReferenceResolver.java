/**
 *
 */
package com.visfresh.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.visfresh.dao.AlertProfileDao;
import com.visfresh.dao.CompanyDao;
import com.visfresh.dao.DeviceDao;
import com.visfresh.dao.LocationProfileDao;
import com.visfresh.dao.NotificationScheduleDao;
import com.visfresh.dao.ShipmentDao;
import com.visfresh.dao.UserDao;
import com.visfresh.entities.AlertProfile;
import com.visfresh.entities.Company;
import com.visfresh.entities.Device;
import com.visfresh.entities.LocationProfile;
import com.visfresh.entities.NotificationSchedule;
import com.visfresh.entities.Shipment;
import com.visfresh.entities.User;
import com.visfresh.io.CompanyResolver;
import com.visfresh.io.ReferenceResolver;
import com.visfresh.io.ShipmentResolver;
import com.visfresh.io.UserResolver;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class DefaultReferenceResolver implements ReferenceResolver, CompanyResolver,
ShipmentResolver, UserResolver {
    @Autowired
    private LocationProfileDao locationProfileDao;
    @Autowired
    private AlertProfileDao alertProfileDao;
    @Autowired
    private NotificationScheduleDao notificationScheduleDao;
    @Autowired
    private DeviceDao deviceDao;
    @Autowired
    private ShipmentDao shipmentDao;
    @Autowired
    private CompanyDao companyDao;
    @Autowired
    private UserDao userDao;

    /**
     * Default constructor.
     */
    public DefaultReferenceResolver() {
        super();
    }

    /* (non-Javadoc)
     * @see com.visfresh.io.ReferenceResolver#getLocationProfile(java.lang.Long)
     */
    @Override
    public LocationProfile getLocationProfile(final Long id) {
        return locationProfileDao.findOne(id);
    }

    /* (non-Javadoc)
     * @see com.visfresh.io.ReferenceResolver#getAlertProfile(java.lang.Long)
     */
    @Override
    public AlertProfile getAlertProfile(final Long id) {
        return alertProfileDao.findOne(id);
    }
    /* (non-Javadoc)
     * @see com.visfresh.io.ReferenceResolver#getNotificationSchedule(java.lang.Long)
     */
    @Override
    public NotificationSchedule getNotificationSchedule(final Long id) {
        return notificationScheduleDao.findOne(id);
    }
    /* (non-Javadoc)
     * @see com.visfresh.io.ReferenceResolver#getDevice(java.lang.String)
     */
    @Override
    public Device getDevice(final String id) {
        return deviceDao.findOne(id);
    }
    /* (non-Javadoc)
     * @see com.visfresh.io.ReferenceResolver#getShipment(java.lang.Long)
     */
    @Override
    public Shipment getShipment(final Long id) {
        return shipmentDao.findOne(id);
    }
    /* (non-Javadoc)
     * @see com.visfresh.io.ReferenceResolver#getCompany(java.lang.Long)
     */
    @Override
    public Company getCompany(final Long id) {
        return companyDao.findOne(id);
    }
    /* (non-Javadoc)
     * @see com.visfresh.io.UserResolver#getUser(java.lang.String)
     */
    @Override
    public User getUser(final Long id) {
        return userDao.findOne(id);
    }
}
