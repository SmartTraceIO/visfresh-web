/**
 *
 */
package com.visfresh.dao.impl;

import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import com.visfresh.dao.DeviceLockDao;
import com.visfresh.entities.SystemMessageType;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class DeviceLockDaoImpl implements DeviceLockDao {
    private static final long MAX_LOCK_TIME = 20 * 60 * 1000l;// 20 minutes

    /**
     * JDBC template.
     */
    @Autowired
    protected NamedParameterJdbcTemplate jdbc;

    /**
     * Default constructor.
     */
    public DeviceLockDaoImpl() {
        super();
    }

    /* (non-Javadoc)
     * @see com.visfresh.dao.DeviceLockDao#lock(java.lang.String, java.lang.String)
     */
    @Override
    public boolean lock(final String device, final String lockKey) {
        final Map<String, Object> params = new HashMap<>();
        params.put("group", device);
        params.put("locker", lockKey);
        params.put("unlockon", new Date(System.currentTimeMillis() + MAX_LOCK_TIME));

        final int result = jdbc.update("insert ignore into grouplocks(`type`, `group`, locker, unlockon)"
                + " values('device', :group, :locker, :unlockon)", params);

        return result > 0;
    }
    /* (non-Javadoc)
     * @see com.visfresh.dao.DeviceLockDao#setUnlockOn(java.lang.String, java.lang.String, java.util.Date)
     */
    @Override
    public void setUnlockOn(final String device, final String lockKey, final Date unlockOn) {
        final Map<String, Object> params = new HashMap<>();
        params.put("group", device);
        params.put("locker", lockKey);
        params.put("unlockon", unlockOn);

        jdbc.update("update grouplocks set unlockon = :unlockon"
                + " where `group` = :group and locker = :locker", params);
    }
    @Override
    public void unlock(final String device, final String lockKey) {
        final Map<String, Object> params = new HashMap<>();
        params.put("group", device);
        params.put("locker", lockKey);

        //delete lock
        jdbc.update("delete from grouplocks where `type` = 'device'"
                + " and `group` = :group and locker = :locker",
                params);
    }

    /* (non-Javadoc)
     * @see com.visfresh.dao.DeviceLockDao#getNotLockedDevicesWithReadyMessages(java.util.Date, int)
     */
    @Override
    public List<String> getNotLockedDevicesWithReadyMessages(final Date retryOn, final int limit) {
        final Map<String, Object> params = new HashMap<>();
        params.put("retryOn", retryOn);
        params.put("limit", limit);
        params.put("messageType", SystemMessageType.Tracker.name());

        final String query = "select sm.group as device from systemmessages sm"
                + "\nwhere sm.type  = :messageType"
                + "\nand sm.retryon <= :retryOn"
                + "\nand not exists (select * from grouplocks l where sm.group = l.group)"
                + "\ngroup by sm.group limit :limit";
        final List<Map<String, Object>> rows = jdbc.queryForList(query, params);

        final List<String> result = new LinkedList<>();
        for (final Map<String,Object> row : rows) {
            result.add((String) row.get("device"));
        }
        return result;
    }
    /* (non-Javadoc)
     * @see com.visfresh.dao.DeviceLockDao#unlockAll(java.util.Date)
     */
    @Override
    public int unlockOlder(final Date beforeDate) {
        final Map<String, Object> params = new HashMap<>();
        params.put("date", beforeDate);
        return jdbc.update("delete from grouplocks where `type` = 'device'"
                + " and unlockon < :date", params);
    }
    /* (non-Javadoc)
     * @see com.visfresh.dao.DeviceLockDao#deleteAll()
     */
    @Override
    public void deleteAll() {
        jdbc.update("delete from grouplocks", new HashMap<>());
    }
}
