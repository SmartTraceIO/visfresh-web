/**
 *
 */
package com.visfresh.dao.impl;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import com.visfresh.dao.ShipmentStatisticsDao;
import com.visfresh.entities.Shipment;
import com.visfresh.io.json.ShipmentStatisticsCollectorSerializer;
import com.visfresh.rules.state.ShipmentStatistics;

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

    private final ShipmentStatisticsCollectorSerializer stateSerializer = new ShipmentStatisticsCollectorSerializer();
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

    @PostConstruct
    public void initCache() {
        cache = new DefaultCache<>("ShipmentStatisticsDao", 10000, 60, 20 * 60);
        cache.initialize();
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
        final Long shipmentId = shipment.getId();
        final ShipmentStatistics session = cache.get(shipmentId);
        if (session != null) {
            return session;
        }

        return getStatistics(shipmentId);
    }

    /**
     * @param shipmentId
     * @return
     */
    protected ShipmentStatistics getStatistics(final Long shipmentId) {
        final ShipmentStatistics session;
        final Map<String, Object> paramMap = new HashMap<String, Object>();
        paramMap.put("shipment", shipmentId);

//        final List<Map<String, Object>> list = jdbc.queryForList(
//                "select state as state from shipmentsessions where shipment = :shipment", paramMap);
//        if (list.size() == 0) {
//            return null;
//        }
//
//        final String state = (String) list.get(0).get("state");
//        session = stateSerializer.parseSession(state);
//        session.setShipmentId(shipmentId);
//
//        if (session != null) {
//            cache.put(shipmentId, session);
//        }
        return null;
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
//        params.put("state", stateSerializer.toString(stats));
        params.put("shipment", stats.getShipmentId());

        if (getStatistics(stats.getShipmentId()) == null) {
            jdbc.update("insert into shipmentsessions(shipment, state) values (:shipment, :state)", params);
        } else {
            jdbc.update("update shipmentsessions set state = :state where shipment = :shipment", params);
        }
        cache.put(stats.getShipmentId(), stats);
    }
}
