/**
 *
 */
package com.visfresh.rules.state;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.visfresh.entities.Location;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class DeviceState {
    private final Map<String, String> properties = new ConcurrentHashMap<String, String>();
    private volatile Location lastLocation;

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
        if (key == null) {
            properties.remove(key);
        } else {
            properties.put(key, value);
        }
    }
    public Set<String> getKeys() {
        return new HashSet<>(properties.keySet());
    }
    /**
     * @return the lastLocation
     */
    public Location getLastLocation() {
        return lastLocation;
    }
    /**
     * @param loc the lastLocation to set
     */
    public void setLastLocation(final Location loc) {
        this.lastLocation = loc;
    }
}
