/**
 *
 */
package au.smarttrace.bt04;

import java.util.Date;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import au.smarttrace.geolocation.impl.dao.BeaconChannelLockDao;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class BeaconChannelLockService {
    private static final long CHANNEL_IDLE_TIME = 20 * 60 * 1000l;

    @Autowired
    protected BeaconChannelLockDao dao;

    /**
     * Default constructor.
     */
    public BeaconChannelLockService() {
        super();
    }

    @Scheduled(fixedDelay = CHANNEL_IDLE_TIME)
    public void clearOldLocks() {
        dao.clearLocksOldestThan(new Date());
    }
    /**
     * @param beacons set of beacon IMEI.
     * @param gateway gateway device.
     * @return set of successfully locked beacon channels.
     */
    public Set<String> lockChannels(final Set<String> beacons, final String gateway) {
        final Date t = new Date(System.currentTimeMillis() + CHANNEL_IDLE_TIME);
        dao.createOrUpdateLocks(beacons, gateway, t);
        return dao.getLocked(gateway, new Date(t.getTime() - 3000l));
    }
}
