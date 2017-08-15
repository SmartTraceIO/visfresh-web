/**
 *
 */
package com.visfresh.dao.impl;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.PreDestroy;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import com.visfresh.dao.ShipmentStatisticsDao;
import com.visfresh.entities.Shipment;
import com.visfresh.io.json.ShipmentStatisticsCollectorSerializer;
import com.visfresh.rules.state.ShipmentStatistics;
import com.visfresh.utils.EntityUtils;
import com.visfresh.utils.SerializerUtils;
import com.visfresh.utils.StringUtils;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class ShipmentStatisticsDaoImpl implements ShipmentStatisticsDao {
    /**
     * Table name.
     */
    public static final String TABLE = "shipmentstats";

    private final ShipmentStatisticsCollectorSerializer serializer = new ShipmentStatisticsCollectorSerializer();
    private DefaultCache<ShipmentStatistics, Long> cache;
    /**
     * JDBC template.
     */
    @Autowired(required = true)
    protected NamedParameterJdbcTemplate jdbc;

    /**
     * Default constructor.
     */
    public ShipmentStatisticsDaoImpl() {
        super();
    }

    @Autowired
    public void initCache(final CacheManagerHolder h) {
        cache = new DefaultCache<>("ShipmentStatisticsDao", 10000, 60, 20 * 60);
        cache.initialize(h);
    }
    @PreDestroy
    public void destroyCache() {
        cache.destroy();
    }
    @Override
    public void clearCache() {
        cache.clear();
    }
    /* (non-Javadoc)
     * @see com.visfresh.dao.ShipmentStatisticsDao#getStatistics(com.visfresh.entities.Shipment)
     */
    @Override
    public ShipmentStatistics getStatistics(final Shipment shipment) {
        final List<Shipment> shipments = new LinkedList<>();
        shipments.add(shipment);

        final List<ShipmentStatistics> stats = getStatistics(shipments);
        return stats.size() == 0 ? null : stats.get(0);
    }
    /* (non-Javadoc)
     * @see com.visfresh.dao.ShipmentStatisticsDao#getStatistics(com.visfresh.entities.Shipment)
     */
    @Override
    public List<ShipmentStatistics> getStatistics(final List<Shipment> shipments) {
        final List<ShipmentStatistics> result = new LinkedList<>();

        final List<Long> shipmentIds = new LinkedList<>(EntityUtils.getIdList(shipments));
        final Iterator<Long> iter = shipmentIds.iterator();
        while (iter.hasNext()) {
            final Long id = iter.next();
            final ShipmentStatistics session = cache.get(id);
            if (session != null) {
                result.add(session);
                iter.remove();
            }
        }

        if (shipmentIds.size() > 0) {
            result.addAll(getStatisticsImpl(shipmentIds));
        }
        return result;
    }

    /**
     * @param shipmentId
     * @return
     */
    protected List<ShipmentStatistics> getStatisticsImpl(final List<Long> shipmentIds) {
        final List<Map<String, Object>> list = jdbc.queryForList(
                "select * from " + TABLE + " where shipment in ("
                        + StringUtils.combine(shipmentIds, ",") + ")", new HashMap<String, Object>());

        final List<ShipmentStatistics> result = new LinkedList<>();

        for (final Map<String, Object> map : list) {
            final ShipmentStatistics stats = createStats(map);
            cache.put(stats.getShipmentId(), stats);
            result.add(stats);
        }

        return result;
    }
    /**
     * @param map map of DB values.
     * @return shipment statistics.
     */
    private ShipmentStatistics createStats(final Map<String, Object> map) {
        final ShipmentStatistics stats = new ShipmentStatistics();

        //shipment ID.
        stats.setShipmentId(((Number) map.get("shipment")).longValue());
        //total time
        stats.setTotalTime(((Number) map.get("total")).longValue());
        //avg temperature
        final Number avg = (Number) map.get("avg");
        if (avg != null) {
            stats.setAvgTemperature(avg.doubleValue());
        }
        //standard devitation
        final Number devitation = (Number) map.get("devitation");
        if (devitation != null) {
            stats.setStandardDevitation(devitation.doubleValue());
        }
        //minimal temperature
        final Number min = (Number) map.get("min");
        if (min != null) {
            stats.setMinimumTemperature(min.doubleValue());
        }
        //maximal temperature
        final Number max = (Number) map.get("max");
        if (max != null) {
            stats.setMaximumTemperature(max.doubleValue());
        }
        //time below lower limit
        stats.setTimeBelowLowerLimit(((Number) map.get("timebelowlimit")).longValue());
        //time above lower limit
        stats.setTimeAboveUpperLimit(((Number) map.get("timeabovelimit")).longValue());
        //collector
        stats.setCollector(serializer.parseShipmentTemperatureStatsCollector(
                SerializerUtils.parseJson((String) map.get("collector"))));

        return stats;
    }
    /* (non-Javadoc)
     * @see com.visfresh.dao.DeviceDao#save(java.lang.String, com.visfresh.rules.DeviceState)
     */
    @Override
    public void saveStatistics(final ShipmentStatistics stats) {
        if (stats.getShipmentId() == null) {
            throw new RuntimeException("Shipment ID for statistics is NULL");
        }

        final Map<String, Object> params = new HashMap<String, Object>();

        //shipment ID.
        params.put("shipment", stats.getShipmentId());
        //total time
        params.put("total", stats.getTotalTime());
        //avg temperature
        params.put("avg", stats.getAvgTemperature());
        //standard devitation
        params.put("devitation", stats.getStandardDevitation());
        //minimal temperature
        params.put("min", stats.getMinimumTemperature());
        //maximal temperature
        params.put("max", stats.getMaximumTemperature());
        //time below lower limit
        params.put("timebelowlimit", stats.getTimeBelowLowerLimit());
        //time above lower limit
        params.put("timeabovelimit", stats.getTimeAboveUpperLimit());
        //collector
        params.put("collector", serializer.toJson(stats.getCollector()).toString());

        jdbc.update(DaoImplBase.createInsertScript(TABLE, new LinkedList<String>(params.keySet()))
                + " ON DUPLICATE KEY UPDATE "
                + createUpdateParts(new HashSet<>(params.keySet())), params);

        cache.put(stats.getShipmentId(), stats);
    }
    /**
     * @param params parameters
     * @return update part of insert script
     */
    private String createUpdateParts(final Set<String> params) {
        params.remove("shipment");
        final List<String> updates = new LinkedList<>();
        for (final String param : params) {
            updates.add(param + " = :" + param);
        }

        return String.join(", ", updates);
    }
}
