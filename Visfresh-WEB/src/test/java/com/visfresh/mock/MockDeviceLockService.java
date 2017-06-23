/**
 *
 */
package com.visfresh.mock;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.visfresh.services.DeviceLockService;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class MockDeviceLockService implements DeviceLockService {
    private Map<String, String> locks = new HashMap<>();

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
    public boolean lockDevice(final String imei, final String lockerId) {
        synchronized (this) {
            if (!locks.containsKey(imei)) {
                locks.put(imei, lockerId);
                return true;
            }
            return false;
        }
    }
    /* (non-Javadoc)
     * @see com.visfresh.services.DeviceLockService#unlock(java.lang.String)
     */
    @Override
    public boolean unlock(final String device, String lockerId) {
        synchronized (this) {
            locks.remove(device);
            return true;
        }
    }
}
