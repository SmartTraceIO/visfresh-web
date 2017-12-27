/**
 *
 */
package com.visfresh.mock;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.visfresh.services.GroupLockService;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class MockDeviceLockService implements GroupLockService {
    private Map<String, String> locks = new HashMap<>();
    private Map<String, Date> unLockTies = new HashMap<>();
    private static final long MAX_LOCK_TIME = 20 * 60 * 1000l;// 20 minutes

    /**
     * Default constructor.
     */
    public MockDeviceLockService() {
        super();
    }

    /* (non-Javadoc)
     * @see com.visfresh.services.DeviceLockService#lockDevice(java.lang.String, java.lang.String)
     */
    @Override
    public boolean lockGroup(final String imei, final String lockerId) {
        synchronized (this) {
            if (!locks.containsKey(imei)) {
                locks.put(imei, lockerId);
                unLockTies.put(imei, new Date(System.currentTimeMillis() + MAX_LOCK_TIME));
                return true;
            }
            return false;
        }
    }
    /* (non-Javadoc)
     * @see com.visfresh.services.DeviceLockService#setUnlockOn(java.lang.String, java.lang.String, java.util.Date)
     */
    @Override
    public void setUnlockOn(final String device, final String lockerId, final Date unlockOn) {
        if (!unLockTies.containsKey(device)) {
            unLockTies.put(device, unlockOn);
        }
    }
    /* (non-Javadoc)
     * @see com.visfresh.services.DeviceLockService#unlock(java.lang.String)
     */
    @Override
    public void unlock(final String device, final String lockerId) {
        synchronized (this) {
            locks.remove(device);
            unLockTies.remove(device);
        }
    }
    /**
     * @return the locks
     */
    public Map<String, String> getLocks() {
        return locks;
    }
    /**
     * @return the unLockTies
     */
    public Map<String, Date> getUnLockTies() {
        return unLockTies;
    }
}
