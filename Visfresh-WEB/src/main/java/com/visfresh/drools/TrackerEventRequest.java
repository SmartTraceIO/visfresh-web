/**
 *
 */
package com.visfresh.drools;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.visfresh.entities.TrackerEvent;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class TrackerEventRequest {
    private final TrackerEvent event;
    private Map<Object, Object> clientProperties = new ConcurrentHashMap<Object, Object>();

    /**
     * @param e tracker event.
     */
    public TrackerEventRequest(final TrackerEvent e) {
        super();
        this.event = e;
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
}
