/**
 *
 */
package com.visfresh.dao.impl;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import com.google.gson.JsonObject;
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
    private static final Logger log = LoggerFactory.getLogger(ShipmentStatisticsDaoImpl.class);
    /**
     * Table name.
     */
    public static final String TABLE = "shipmentstats";

    private final ShipmentStatisticsCollectorSerializer serializer = new ShipmentStatisticsCollectorSerializer();
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

        try {
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
        } catch (final DataAccessException e) {
            log.error("Failed to save shipment statistic: " + createStatsDump(stats), e);
        }
    }

    /**
     * @param stats
     * @return
     */
    private static String createStatsDump(final ShipmentStatistics stats) {
        final StringBuilder sb = new StringBuilder();
        sb.append("Time above limit: ").append(stats.getTimeAboveUpperLimit()).append('\n');
        sb.append("Time below limit: ").append(stats.getTimeBelowLowerLimit()).append('\n');
        sb.append("Total time: ").append(stats.getTotalTime()).append('\n');
        sb.append("AVG temperature: ").append(stats.getAvgTemperature()).append('\n');
        sb.append("Maximum temperature: ").append(stats.getMaximumTemperature()).append('\n');
        sb.append("Minimum temperature: ").append(stats.getMinimumTemperature()).append('\n');
        sb.append("Shipment ID: ").append(stats.getShipmentId()).append('\n');
        sb.append("Standard devitation: ").append(stats.getStandardDevitation()).append('\n');

        final JsonObject json = new ShipmentStatisticsCollectorSerializer().toJson(stats.getCollector());
        sb.append(": ").append(json).append('\n');
        return sb.toString();
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
