/**
 *
 */
package com.visfresh.mock;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.visfresh.dao.NotificationScheduleDao;
import com.visfresh.entities.AlertProfile;
import com.visfresh.entities.Company;
import com.visfresh.entities.Device;
import com.visfresh.entities.LocationProfile;
import com.visfresh.entities.NotificationSchedule;
import com.visfresh.entities.Shipment;
import com.visfresh.io.ReferenceResolver;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class MockReferenceResolver implements ReferenceResolver {
    @Autowired
    private MockRestService restService;
    @Autowired
    private NotificationScheduleDao notificationScheduleDao;

    /**
     * Fefault constructor.
     */
    public MockReferenceResolver() {
        super();
    }

    /* (non-Javadoc)
     * @see com.visfresh.controllers.ReferenceResolver#getLocationProfile(java.lang.Long)
     */
    @Override
    public LocationProfile getLocationProfile(final Long id) {
        return restService.locationProfiles.get(id);
    }
    /* (non-Javadoc)
     * @see com.visfresh.controllers.ReferenceResolver#getAlertProfile(java.lang.Long)
     */
    @Override
    public AlertProfile getAlertProfile(final Long id) {
        return restService.alertProfiles.get(id);
    }
    /* (non-Javadoc)
     * @see com.visfresh.controllers.ReferenceResolver#getNotificationSchedule(java.lang.Long)
     */
    @Override
    public NotificationSchedule getNotificationSchedule(final Long id) {
        return notificationScheduleDao.findOne(id);
    }
    /* (non-Javadoc)
     * @see com.visfresh.controllers.ReferenceResolver#getDevice(java.lang.String)
     */
    @Override
    public Device getDevice(final String imei) {
        return restService.devices.get(imei);
    }
    /* (non-Javadoc)
     * @see com.visfresh.io.ReferenceResolver#getShipment(java.lang.Long)
     */
    @Override
    public Shipment getShipment(final Long id) {
        return restService.shipments.get(id);
    }
    /* (non-Javadoc)
     * @see com.visfresh.io.ReferenceResolver#getCompany(java.lang.Long)
     */
    @Override
    public Company getCompany(final Long id) {
        return restService.companies.get(id);
    }
}
