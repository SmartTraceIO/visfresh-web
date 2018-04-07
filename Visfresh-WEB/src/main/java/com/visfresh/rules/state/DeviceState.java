/**
 *
 */
package com.visfresh.rules.state;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class DeviceState {
    private final Map<String, String> properties = new ConcurrentHashMap<String, String>();

    /**
     * Default constructor.
     */
    public DeviceState() {
        super();
    }

    public String getProperty(final String key) {
        return properties.get(key);
    }
    public void setProperty(final String key, final String value) {
        if (value == null) {
            properties.remove(key);
        } else {
            properties.put(key, value);
        }
    }

    public Set<String> getKeys() {
        return new HashSet<>(properties.keySet());
    }
}
