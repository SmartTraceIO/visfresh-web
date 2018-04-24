/**
 *
 */
package com.visfresh.impl.services;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.visfresh.dao.GroupLockDao;
import com.visfresh.services.GroupLockService;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class GroupLockServiceImpl implements GroupLockService {
    private static Logger log = LoggerFactory.getLogger(GroupLockServiceImpl.class);

    private static final long TIME_OUT = 15 * 1000l; // 15 seconds

    @Autowired
    private GroupLockDao dao;
    private final String instanceId;

    /**
     * Default constructor.
     */
    @Autowired
    public GroupLockServiceImpl(final Environment env) {
        super();
        instanceId = env.getProperty("instance.id");
    }

    /**
     *
     */
    @Scheduled(fixedDelay = TIME_OUT)
    public void unlockOlds() {
        log.debug("Started schedule for unlock older looked devices");
        final int unlocked = dao.unlockOlder(new Date());
        if (unlocked > 0) {
            log.debug(unlocked + " locks have unlocked by device lock service");
        }
        log.debug("Finished schedule for unlock older looked devices");
    }

    /* (non-Javadoc)
     * @see com.visfresh.services.GroupLockService#lockGroup(java.lang.String, java.lang.String)
     */
    @Override
    public boolean lockGroup(final String group, final String lockerId) {
        return dao.lock(group, createLockKey(lockerId));
    }
    /* (non-Javadoc)
     * @see com.visfresh.services.GroupLockService#setUnlockOn(java.lang.String, java.lang.String, java.util.Date)
     */
    @Override
    public void setUnlockOn(final String group, final String lockerId, final Date unlockOn) {
        dao.setUnlockOn(group, createLockKey(lockerId), unlockOn);
    }
    /* (non-Javadoc)
     * @see com.visfresh.services.GroupLockService#unlock(java.lang.String)
     */
    @Override
    public void unlock(final String group, final String lockerId) {
        dao.unlock(group, createLockKey(lockerId));
    }

    /**
     * @param lockerId the locker ID.
     * @return full lock key include instance ID.
     */
    private String createLockKey(final String lockerId) {
        return instanceId + "-" + lockerId;
    }
}
