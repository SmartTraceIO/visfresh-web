/**
 *
 */
package au.smarttrace.geolocation.impl;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import au.smarttrace.geolocation.impl.dao.BeaconChannelLockDao;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class BeaconChannelUnlocker {
    public static final long CHANNEL_IDLE_TIME = 20 * 60 * 1000l;

    @Autowired
    private BeaconChannelLockDao dao;

    /**
     * Default constructor.
     */
    public BeaconChannelUnlocker() {
        super();
    }
    @Scheduled(fixedDelay = CHANNEL_IDLE_TIME)
    public void clearOldLocks() {
        dao.clearLocksOldestThan(new Date());
    }
}
