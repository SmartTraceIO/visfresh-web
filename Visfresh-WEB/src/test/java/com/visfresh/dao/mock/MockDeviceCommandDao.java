/**
 *
 */
package com.visfresh.dao.mock;

import org.springframework.stereotype.Component;

import com.visfresh.dao.DeviceCommandDao;
import com.visfresh.entities.DeviceCommand;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class MockDeviceCommandDao extends MockDaoBase<DeviceCommand, Long>
        implements DeviceCommandDao {
    /**
     * Default constructor.
     */
    public MockDeviceCommandDao() {
        super();
    }
    /* (non-Javadoc)
     * @see com.visfresh.dao.mock.MockDaoBase#getValueForFilterOrCompare(java.lang.String, com.visfresh.entities.EntityWithId)
     */
    @Override
    protected Object getValueForFilterOrCompare(final String property, final DeviceCommand t) {
        return null;
    }
}
