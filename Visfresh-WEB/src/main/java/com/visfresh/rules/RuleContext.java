/**
 *
 */
package com.visfresh.rules;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.visfresh.entities.TrackerEvent;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class RuleContext {
    private final TrackerEvent event;
    private Map<Object, Object> clientProperties = new ConcurrentHashMap<Object, Object>();
    private Map<TrackerEventRule, Boolean> processedMap = new HashMap<TrackerEventRule, Boolean>();

    /**
     * @param e tracker event.
     */
    public RuleContext(final TrackerEvent e) {
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
}
