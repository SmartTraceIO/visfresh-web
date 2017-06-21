/**
 *
 */
package com.visfresh.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import com.visfresh.dao.DeviceLockDao;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class DefaultDeviceLockService implements DeviceLockService {
    @Autowired
    private DeviceLockDao dao;
    private final String instanceId;

    /**
     * Default constructor.
     */
    @Autowired
    public DefaultDeviceLockService(final Environment env) {
        super();
        instanceId = env.getProperty("instance.id");
    }

    /* (non-Javadoc)
     * @see com.visfresh.services.DeviceLockService#lockGroup(java.lang.String, java.lang.String)
     */
    @Override
    public boolean lockDevice(final String imei, final String lockerId) {
        return dao.lock(imei, createLockKey(lockerId));
    }

    /**
     * @param lockerId
     * @return
     */
    private String createLockKey(final String lockerId) {
        return instanceId + "-" + lockerId;
    }

    /* (non-Javadoc)
     * @see com.visfresh.services.DeviceLockService#unlock(java.lang.String)
     */
    @Override
    public boolean unlock(final String imei) {
        return dao.unlockIfNoMessages(imei, instanceId);
    }
}
