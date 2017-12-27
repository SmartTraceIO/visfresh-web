/**
 *
 */
package com.visfresh.dao.impl;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import com.visfresh.dao.GroupLockDao;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class GroupLockDaoImpl implements GroupLockDao {
    private static final long MAX_LOCK_TIME = 20 * 60 * 1000l;// 20 minutes

    /**
     * JDBC template.
     */
    @Autowired
    protected NamedParameterJdbcTemplate jdbc;

    /**
     * Default constructor.
     */
    public GroupLockDaoImpl() {
        super();
    }

    /* (non-Javadoc)
     * @see com.visfresh.dao.GroupLockDao#lock(java.lang.String, java.lang.String)
     */
    @Override
    public boolean lock(final String grp, final String lockKey) {
        final Map<String, Object> params = new HashMap<>();
        params.put("group", grp);
        params.put("locker", lockKey);
        params.put("unlockon", new Date(System.currentTimeMillis() + MAX_LOCK_TIME));

        //the lock type is set to 'any' for now and will ignored in future, because
        //the type can be encoded to group name
        final int result = jdbc.update("insert ignore into grouplocks(`type`, `group`, locker, unlockon)"
                + " values('any', :group, :locker, :unlockon)", params);

        return result > 0;
    }
    /* (non-Javadoc)
     * @see com.visfresh.dao.GroupLockDao#setUnlockOn(java.lang.String, java.lang.String, java.util.Date)
     */
    @Override
    public void setUnlockOn(final String group, final String lockKey, final Date unlockOn) {
        final Map<String, Object> params = new HashMap<>();
        params.put("group", group);
        params.put("locker", lockKey);
        params.put("unlockon", unlockOn);

        jdbc.update("update grouplocks set unlockon = :unlockon"
                + " where `group` = :group and locker = :locker", params);
    }
    @Override
    public void unlock(final String group, final String lockKey) {
        final Map<String, Object> params = new HashMap<>();
        params.put("group", group);
        params.put("locker", lockKey);

        //delete lock
        jdbc.update("delete from grouplocks where `group` = :group and locker = :locker",
                params);
    }
    /* (non-Javadoc)
     * @see com.visfresh.dao.GroupLockDao#unlockAll(java.util.Date)
     */
    @Override
    public int unlockOlder(final Date beforeDate) {
        final Map<String, Object> params = new HashMap<>();
        params.put("date", beforeDate);
        return jdbc.update("delete from grouplocks where unlockon < :date", params);
    }
    /* (non-Javadoc)
     * @see com.visfresh.dao.GroupLockDao#deleteAll()
     */
    @Override
    public void deleteAll() {
        jdbc.update("delete from grouplocks", new HashMap<>());
    }
}
