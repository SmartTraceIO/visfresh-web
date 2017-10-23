/**
 *
 */
package com.visfresh.impl.singleshipment;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.visfresh.entities.Location;
import com.visfresh.io.shipment.SingleShipmentData;
import com.visfresh.rules.state.ShipmentSession;
import com.visfresh.services.LocationService;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class SingleShipmentBuildContext {
    private SingleShipmentData data;
    private Map<Long, ShipmentSession> sessions = new HashMap<>();
    private final LocationService locSrv;

    /**
     * Default constructor.
     */
    public SingleShipmentBuildContext(final LocationService locSrv) {
        super();
        this.locSrv = locSrv;
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
    /**
     * @param loc location.
     * @return location description.
     */
    public String getLocationDescription(final Location loc) {
        return locSrv.getLocationDescription(loc);
    }
}
