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

import com.visfresh.dao.DeviceLockDao;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class DeviceLockDaoImpl implements DeviceLockDao {
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
        params.put("lastupdate", new Date());

        final int result = jdbc.update("insert ignore into grouplocks(`type`, `group`, locker, lastupdate)"
                + " values('device', :group, :locker, :lastupdate)", params);

        return result > 0;
    }

    /* (non-Javadoc)
     * @see com.visfresh.dao.DeviceLockDao#unlockIfNoMessages(java.lang.String, java.lang.String)
     */
    @Override
    public boolean unlockIfNoMessages(final String device, final String lockKey) {
        final Map<String, Object> params = new HashMap<>();
        params.put("group", device);
        params.put("locker", lockKey);
        params.put("lastupdate", new Date());

        //delete lock
        jdbc.update("delete from grouplocks where `type` = 'device'"
                + " and `group` = :group and locker = :locker"
                + " and not exists (select * from systemmessages where systemmessages.group = :group"
                + " and systemmessages.retryon < :lastupdate)",
                params);
        return jdbc.update("update grouplocks set lastupdate = :lastupdate where"
                + " `type` = 'device' and `group` = :group and locker = :locker", params) == 0;
    }

    /* (non-Javadoc)
     * @see com.visfresh.dao.DeviceLockDao#unlockAll(java.util.Date)
     */
    @Override
    public int unlockOlder(final Date beforeDate) {
        final Map<String, Object> params = new HashMap<>();
        params.put("date", beforeDate);
        return jdbc.update("delete from grouplocks where `type` = 'device'"
                + " and lastupdate < :date", params);
    }
    /* (non-Javadoc)
     * @see com.visfresh.dao.DeviceLockDao#deleteAll()
     */
    @Override
    public void deleteAll() {
        jdbc.update("delete from grouplocks", new HashMap<>());
    }
}
