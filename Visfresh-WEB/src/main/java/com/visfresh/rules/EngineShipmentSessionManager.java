/**
 *
 */
package com.visfresh.rules;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.visfresh.dao.ShipmentSessionDao;
import com.visfresh.entities.Shipment;
import com.visfresh.rules.state.ShipmentSession;
import com.visfresh.rules.state.ShipmentSessionManager;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class EngineShipmentSessionManager implements ShipmentSessionManager {
    private static final String ruleEngineCacheId = "ruleEngine";
    private static final Logger log = LoggerFactory.getLogger(EngineShipmentSessionManager.class);

    protected final Map<Long, ShipmentSessionCacheEntry> sessionCache = new ConcurrentHashMap<>();
    @Autowired
    private ShipmentSessionDao shipmentSessionDao;

    private static class ShipmentSessionCacheEntry {
        ShipmentSession session;
        final Map<String, Object> loaders = new ConcurrentHashMap<>();
    }

    /**
     * Default constructor.
     */
    public EngineShipmentSessionManager() {
        super();
    }
    /* (non-Javadoc)
     * @see com.visfresh.rules.state.ShipmentSessionManager#getSession(com.visfresh.entities.Shipment)
     */
    @Override
    public ShipmentSession getSession(final Shipment s) {
        return loadSession(s, ruleEngineCacheId);
    }
    /**
     * @param s shipment
     * @param loaderId
     * @return shipment session.
     */
    public ShipmentSession loadSession(final Shipment s, final String loaderId) {
        //load cache entry
        ShipmentSessionCacheEntry entry;
        synchronized (sessionCache) {
            entry = sessionCache.get(s.getId());
            if (entry == null) {
                entry = new ShipmentSessionCacheEntry();
                sessionCache.put(s.getId(), entry);
                entry.loaders.put(loaderId, this);
            }
        }

        //load session.
        synchronized (entry) {
            if (entry.session == null) {
                entry.session = loadSessionFromDb(s);
                log.debug("Shipment session for " + s.getId() + " is load from DB");
            }
            if (entry.session == null) {
                entry.session = new ShipmentSession(s.getId());
            }
        }

        return entry.session;
    }

    public void unloadSession(final Shipment s, final boolean saveSession) {
        unloadSession(s, ruleEngineCacheId, saveSession);
    }

    public void unloadSession(final Shipment s, final String loaderId, final boolean saveSession) {
        ShipmentSessionCacheEntry ss;
        synchronized (sessionCache) {
            ss = sessionCache.get(s.getId());
        }

        if (ss != null) {
            synchronized (ss) {
                ss.loaders.remove(loaderId);
                if (saveSession) {
                    saveSessionToDb(s, ss.session);
                    log.debug("Shipment session for " + s.getId() + " has saved");
                }

                if (ss.loaders.isEmpty()) {
                    synchronized (sessionCache) {
                        sessionCache.remove(s.getId());
                        log.debug("Shipment session cache for " + s.getId() + " has cleaned");
                    }
                }
            }
        }
    }

    /**
     * @param s
     * @param ss
     */
    protected void saveSessionToDb(final Shipment s, final ShipmentSession ss) {
        shipmentSessionDao.saveSession(s, ss);
    }
    /**
     * @param s
     * @return
     */
    protected ShipmentSession loadSessionFromDb(final Shipment s) {
        return shipmentSessionDao.getSession(s);
    }
}
