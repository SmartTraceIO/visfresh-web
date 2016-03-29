/**
 *
 */
package com.visfresh.rules;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.visfresh.entities.Location;
import com.visfresh.entities.TrackerEvent;
import com.visfresh.rules.state.ShipmentSessionManager;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class RuleContext {
    private final TrackerEvent event;
    private final ShipmentSessionManager state;
    private Map<Object, Object> clientProperties = new ConcurrentHashMap<Object, Object>();
    private Map<TrackerEventRule, Boolean> processedMap = new HashMap<TrackerEventRule, Boolean>();
    private Location oldLocation;

    /**
     * @param e tracker event.
     * @param state device rules state.
     */
    public RuleContext(final TrackerEvent e, final ShipmentSessionManager state) {
        super();
        this.event = e;
        this.state = state;
    }
    /**
     * @return the event
     */
    public TrackerEvent getEvent() {
        return event;
    }
    /**
     * @param key property key.
     * @param value property value.
     */
    public void putClientProperty(final Object key, final Object value) {
        clientProperties.put(key, value);
    }
    /**
     * @param key property key.
     * @return property value.
     */
    public Object getClientProperty(final Object key) {
        return clientProperties.get(key);
    }
    /**
     * @param rule rule.
     */
    public void setProcessed(final TrackerEventRule rule) {
        synchronized (processedMap) {
            processedMap.put(rule, Boolean.TRUE);
        }
    }
    /**
     * @param rule rule.
     * @return
     */
    public boolean isProcessed(final TrackerEventRule rule) {
        synchronized (processedMap) {
            return Boolean.TRUE == processedMap.get(rule);
        }
    }
    /**
     * @return the session
     */
    public ShipmentSessionManager getSessionManager() {
        return state;
    }
    /**
     * @param oldLocation the oldLocation to set
     */
    public void setOldLocation(final Location oldLocation) {
        this.oldLocation = oldLocation;
    }
    /**
     * @return the oldLocation
     */
    public Location getOldLocation() {
        return oldLocation;
    }
}
