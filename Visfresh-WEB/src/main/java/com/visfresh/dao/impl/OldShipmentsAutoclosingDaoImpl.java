/**
 *
 */
package com.visfresh.dao.impl;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import com.visfresh.dao.OldShipmentsAutoclosingDao;
import com.visfresh.utils.StringUtils;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class OldShipmentsAutoclosingDaoImpl implements OldShipmentsAutoclosingDao {
    @Autowired
    protected NamedParameterJdbcTemplate jdbc;

    /**
     * Default constructor.
     */
    public OldShipmentsAutoclosingDaoImpl() {
        super();
    }

    /* (non-Javadoc)
     * @see com.visfresh.dao.OldShipmentsAutoclosingDao#findNotClosedShipmentsWithInactiveDevices(int)
     */
    @Override
    public List<Long> findNotClosedShipmentsWithInactiveDevices(final int limit) {
        final String query = "select id from shipments"
                + " join devices on shipments.device = devices.imei and not devices.active"
                + " where status <> 'Ended' and status <> 'Arrived' order by id limit " + limit;
        final List<Map<String, Object>> rows = jdbc.queryForList(query, new HashMap<>());
        final List<Long> ids = new LinkedList<>();
        for (final Map<String,Object> row : rows) {
            ids.add(((Number) row.get("id")).longValue());
        }
        return ids;
    }

    /* (non-Javadoc)
     * @see com.visfresh.dao.OldShipmentsAutoclosingDao#closeShipments(java.util.Collection)
     */
    @Override
    public int closeShipments(final Collection<Long> ids) {
        if (ids.isEmpty()) {
            return 0;
        }

        final String query = "update shipments set status = 'Ended' where id in ("
                + StringUtils.combine(ids, ", ") + ")";

        return jdbc.update(query, new HashMap<>());
    }
}
