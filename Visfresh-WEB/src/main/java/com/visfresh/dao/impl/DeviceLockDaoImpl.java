/**
 *
 */
package com.visfresh.dao.impl;

import java.util.Date;

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
     *
     */
    public DeviceLockDaoImpl() {
        // TODO Auto-generated constructor stub
    }

    /* (non-Javadoc)
     * @see com.visfresh.dao.DeviceLockDao#lock(java.lang.String, java.lang.String)
     */
    @Override
    public boolean lock(final String device, final String lockKey) {
        // TODO Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
     * @see com.visfresh.dao.DeviceLockDao#unlockIfNoMessages(java.lang.String, java.lang.String)
     */
    @Override
    public boolean unlockIfNoMessages(final String device, final String lockKeyPrefix) {
        // TODO Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
     * @see com.visfresh.dao.DeviceLockDao#unlockAll(java.util.Date)
     */
    @Override
    public void unlockAll(final Date beforeDate) {
        // TODO Auto-generated method stub

    }

}
