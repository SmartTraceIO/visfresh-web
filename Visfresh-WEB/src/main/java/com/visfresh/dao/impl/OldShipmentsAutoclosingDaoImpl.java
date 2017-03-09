/**
 *
 */
package com.visfresh.dao.impl;

import java.util.Collection;
import java.util.Date;
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
     * @see com.visfresh.dao.OldShipmentsAutoclosingDao#findActiveShipmentsFor(java.util.List)
     */
    @Override
    public Map<String, List<Long>> findActiveShipmentsFor(final List<String> devices) {
        if (devices.isEmpty()) {
            return new HashMap<>();
        }

        final String query = "select id, device from shipments"
                + " where device in ("
                + StringUtils.combine(devices, ",")
                + ") and status <> 'Ended' and status <> 'Arrived'";
        final List<Map<String, Object>> rows = jdbc.queryForList(query, new HashMap<>());

        final Map<String, List<Long>> deviceShipments = new HashMap<>();

        for (final Map<String,Object> row : rows) {
            final String device = (String) row.get("device");
            final Long shipment = ((Number) row.get("id")).longValue();

            List<Long> shipments = deviceShipments.get(device);
            if (shipments == null) {
                shipments = new LinkedList<>();
                deviceShipments.put(device, shipments);
            }

            shipments.add(shipment);
        }

        return deviceShipments;
    }
    /* (non-Javadoc)
     * @see com.visfresh.dao.OldShipmentsAutoclosingDao#findDevicesWithoutReadingsAfter(java.util.Date)
     */
    @Override
    public List<String> findDevicesWithoutReadingsAfter(final Date date) {
        final String query = "select d.imei as device, max(te.time) as maxtime, d.active as isactive "
                + "from devices d "
                + "left outer join trackerevents te on d.imei = te.device "
                + "group by d.imei having (maxtime < :date or maxtime is NULL) and isactive";

        final HashMap<String, Object> params = new HashMap<>();
        params.put("date", date);

        final List<Map<String, Object>> rows = jdbc.queryForList(query, params);
        final List<String> devices = new LinkedList<>();
        for (final Map<String,Object> row : rows) {
            devices.add((String) row.get("device"));
        }

        return devices;
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
