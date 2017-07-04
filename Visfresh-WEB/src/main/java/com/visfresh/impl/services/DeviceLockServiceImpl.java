/**
 *
 */
package com.visfresh.impl.services;

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
import com.visfresh.services.DeviceLockService;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class DeviceLockServiceImpl implements DeviceLockService {
    private static Logger log = LoggerFactory.getLogger(DeviceLockServiceImpl.class);

    private static final long TIME_OUT = 15 * 1000l; // 15 seconds

    @Autowired
    private DeviceLockDao dao;
    private final String instanceId;

    private final Timer timer = new Timer("Unlock old lockings");

    /**
     * Default constructor.
     */
    @Autowired
    public DeviceLockServiceImpl(final Environment env) {
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
        final int unlocked = dao.unlockOlder(new Date());
        if (unlocked > 0) {
            log.debug(unlocked + " locks have unlocked by device lock service");
        }
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
     * @see com.visfresh.services.DeviceLockService#setUnlockOn(java.lang.String, java.lang.String, java.util.Date)
     */
    @Override
    public void setUnlockOn(final String device, final String lockerId, final Date unlockOn) {
        dao.setUnlockOn(device, createLockKey(lockerId), unlockOn);
    }
    /* (non-Javadoc)
     * @see com.visfresh.services.DeviceLockService#unlock(java.lang.String)
     */
    @Override
    public void unlock(final String device, final String lockerId) {
        dao.unlock(device, createLockKey(lockerId));
    }

    /**
     * @param lockerId the locker ID.
     * @return full lock key include instance ID.
     */
    private String createLockKey(final String lockerId) {
        return instanceId + "-" + lockerId;
    }
}
