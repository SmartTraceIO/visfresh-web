/**
 *
 */
package com.visfresh.dao.mock;

import org.springframework.stereotype.Component;

import com.visfresh.controllers.DeviceConstants;
import com.visfresh.dao.DeviceDao;
import com.visfresh.entities.Device;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class MockDeviceDao extends MockEntityWithCompanyDaoBase<Device, String> implements DeviceDao {

    /* (non-Javadoc)
     * @see com.visfresh.dao.DeviceDao#findAllByImei(java.lang.String)
     */
    @Override
    public Device findByImei(final String imei) {
        return findOne(imei);
    }
    /* (non-Javadoc)
     * @see com.visfresh.dao.mock.MockDaoBase#getValueForFilterOrCompare(java.lang.String, com.visfresh.entities.EntityWithId)
     */
    @Override
    protected Object getValueForFilterOrCompare(final String property, final Device t) {
        if (property.equals(DeviceConstants.PROPERTY_DESCRIPTION)) {
            return t.getDescription();
        }
        if (property.equals(DeviceConstants.PROPERTY_NAME)) {
            return t.getName();
        }
        if (property.equals(DeviceConstants.PROPERTY_SN)) {
            return t.getSn();
        }
        if (property.equals(DeviceConstants.PROPERTY_IMEI)) {
            return t.getImei();
        }
        throw new IllegalArgumentException("Unsupported property: " + property);
    }
}
