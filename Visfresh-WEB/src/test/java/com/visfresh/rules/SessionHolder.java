/**
 *
 */
package com.visfresh.rules;

import java.util.HashMap;
import java.util.Map;

import com.visfresh.entities.Shipment;
import com.visfresh.rules.state.ShipmentSession;
import com.visfresh.rules.state.ShipmentSessionManager;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class SessionHolder implements ShipmentSessionManager {
    private Map<Long, ShipmentSession> sessions = new HashMap<>();

    /**
     *
     */
    public SessionHolder(final Shipment s, final ShipmentSession session) {
        super();
        session.setShipmentId(s.getId());
        sessions.put(s.getId(), session);
    }
    /**
     *
     */
    public SessionHolder(final Shipment s) {
        this(s, new ShipmentSession(s.getId()));
    }
    /**
     *
     */
    public SessionHolder() {
        super();
    }
    /*
     * (non-Javadoc)
     *
     * @see
     * com.visfresh.rules.state.ShipmentSessionManager#getSession(com.visfresh
     * .entities.Shipment)
     */
    @Override
    public ShipmentSession getSession(final Shipment s) {
        ShipmentSession ss = sessions.get(s.getId());
        if (ss == null) {
            ss = new ShipmentSession(s.getId());
            sessions.put(s.getId(), ss);
        }
        return ss;
    }
}
