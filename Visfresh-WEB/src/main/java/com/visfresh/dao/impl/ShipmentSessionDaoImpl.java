/**
 *
 */
package com.visfresh.dao.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import com.visfresh.dao.ShipmentSessionDao;
import com.visfresh.entities.Shipment;
import com.visfresh.io.json.ShipmentSessionSerializer;
import com.visfresh.rules.state.ShipmentSession;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class ShipmentSessionDaoImpl implements ShipmentSessionDao {
    /**
     * Table name.
     */
    public static final String TABLE = "shipmentsessions";
    private final ShipmentSessionSerializer stateSerializer = new ShipmentSessionSerializer();
    private DefaultCache<ShipmentSession, Long> cache;
    /**
     * JDBC template.
     */
    @Autowired(required = true)
    protected NamedParameterJdbcTemplate jdbc;

    /**
     * Default constructor.
     */
    public ShipmentSessionDaoImpl() {
        super();
    }

    @PostConstruct
    public void initCache() {
        cache = new DefaultCache<>("ShipmentSessionDao", 10000, 60, 20 * 60);
        cache.initialize();
    }
    @PreDestroy
    public void destroyCache() {
        cache.destroy();
    }

    /* (non-Javadoc)
     * @see com.visfresh.dao.DeviceDao#getState(java.lang.String)
     */
    @Override
    public ShipmentSession getSession(final Shipment shipment) {
        final Long shipmentId = shipment.getId();
        final ShipmentSession session = cache.get(shipmentId);
        if (session != null) {
            return session;
        }

        return getSession(shipmentId);
    }

    /**
     * @param shipmentId
     * @return
     */
    protected ShipmentSession getSession(final Long shipmentId) {
        ShipmentSession session;
        final Map<String, Object> paramMap = new HashMap<String, Object>();
        paramMap.put("shipment", shipmentId);

        final List<Map<String, Object>> list = jdbc.queryForList(
                "select state as state from shipmentsessions where shipment = :shipment", paramMap);
        if (list.size() == 0) {
            return null;
        }

        final String state = (String) list.get(0).get("state");
        session = stateSerializer.parseSession(state);
        session.setShipmentId(shipmentId);

        if (session != null) {
            cache.put(shipmentId, session);
        }
        return session;
    }
    /* (non-Javadoc)
     * @see com.visfresh.dao.DeviceDao#save(java.lang.String, com.visfresh.rules.DeviceState)
     */
    @Override
    public void saveSession(final ShipmentSession session) {
        if (session.getShipmentId() == null) {
            throw new RuntimeException("Shipment ID for session is NULL");
        }

        final Map<String, Object> params = new HashMap<String, Object>();
        params.put("state", stateSerializer.toString(session));
        params.put("shipment", session.getShipmentId());

        if (getSession(session.getShipmentId()) == null) {
            jdbc.update("insert into shipmentsessions(shipment, state) values (:shipment, :state)", params);
        } else {
            jdbc.update("update shipmentsessions set state = :state where shipment = :shipment", params);
        }
        cache.put(session.getShipmentId(), session);
    }
}
