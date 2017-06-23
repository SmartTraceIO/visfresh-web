/**
 *
 */
package com.visfresh.services;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private static Logger log = LoggerFactory.getLogger(DefaultDeviceLockService.class);

    private static final long TIME_OUT = 15 * 60 * 1000l; // 15 minutes
    private static final long MAX_LOCK_TIME = 20 * 60 * 1000l;// 20 minutes

    @Autowired
    private DeviceLockDao dao;
    private final String instanceId;

    private final Timer timer = new Timer("Unlock old lockings");

    /**
     * Default constructor.
     */
    @Autowired
    public DefaultDeviceLockService(final Environment env) {
        super();
        instanceId = env.getProperty("instance.id");
    }

    @PostConstruct
    public void init() {
        timer.schedule(new TimerTask() {

            @Override
            public void run() {
                unlockOlds();

            }
        }, TIME_OUT, TIME_OUT);
    }
    /**
     *
     */
    protected void unlockOlds() {
        log.debug("Ustarted of unlocking very old locks if found");
        dao.unlockOlder(new Date(System.currentTimeMillis() - MAX_LOCK_TIME));
        log.debug("Finished of unlocking very old locks");
    }

    @PreDestroy
    public void shutdown() {
        timer.cancel();
    }

    /* (non-Javadoc)
     * @see com.visfresh.services.DeviceLockService#lockGroup(java.lang.String, java.lang.String)
     */
    @Override
    public boolean lockDevice(final String device, final String lockerId) {
        return dao.lock(device, createLockKey(lockerId));
    }
    /* (non-Javadoc)
     * @see com.visfresh.services.DeviceLockService#unlock(java.lang.String)
     */
    @Override
    public boolean unlock(final String device, final String lockerId) {
        return dao.unlockIfNoMessages(device, createLockKey(lockerId));
    }

    /**
     * @param lockerId the locker ID.
     * @return full lock key include instance ID.
     */
    private String createLockKey(final String lockerId) {
        return instanceId + "-" + lockerId;
    }
}
