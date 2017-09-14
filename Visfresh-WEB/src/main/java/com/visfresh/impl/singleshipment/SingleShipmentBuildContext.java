/**
 *
 */
package com.visfresh.impl.singleshipment;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.visfresh.io.shipment.SingleShipmentData;
import com.visfresh.rules.state.ShipmentSession;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class SingleShipmentBuildContext {
    private SingleShipmentData data;
    private Map<Long, ShipmentSession> sessions = new HashMap<>();

    /**
     * Default constructor.
     */
    public SingleShipmentBuildContext() {
        super();
    }

    /**
     * @return the data
     */
    public SingleShipmentData getData() {
        return data;
    }
    /**
     * @param data the data to set
     */
    public void setData(final SingleShipmentData data) {
        this.data = data;
    }
    /**
     * @return the sessions
     */
    public List<ShipmentSession> getSessions() {
        return new LinkedList<>(sessions.values());
    }
    public ShipmentSession getSession(final Long id) {
        return sessions.get(id);
    }
    public void addSession(final ShipmentSession s) {
        sessions.put(s.getShipmentId(), s);
    }
}
