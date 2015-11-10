/**
 *
 */
package com.visfresh.dao.mock;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.visfresh.controllers.DeviceConstants;
import com.visfresh.dao.DeviceDao;
import com.visfresh.dao.impl.DeviceStateSerializer;
import com.visfresh.entities.Device;
import com.visfresh.rules.DeviceState;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class MockDeviceDao extends MockEntityWithCompanyDaoBase<Device, String> implements DeviceDao {
    private final DeviceStateSerializer serializer = new DeviceStateSerializer();
    private final Map<String, String> deviceStates = new HashMap<String, String>();

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
    /* (non-Javadoc)
     * @see com.visfresh.dao.mock.MockDaoBase#delete(java.io.Serializable)
     */
    @Override
    public void delete(final String id) {
        super.delete(id);
        deviceStates.remove(id);
    }
    /* (non-Javadoc)
     * @see com.visfresh.dao.DeviceDao#save(java.lang.String, com.visfresh.rules.DeviceState)
     */
    @Override
    public void saveState(final String imei, final DeviceState state) {
        deviceStates.put(imei, serializer.toString(state));
    }
    /* (non-Javadoc)
     * @see com.visfresh.dao.DeviceDao#getState(java.lang.String)
     */
    @Override
    public DeviceState getState(final String imei) {
        final String str = deviceStates.get(imei);
        return str == null ? null : serializer.parseState(str);
    }
}
