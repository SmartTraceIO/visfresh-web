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

    public String getProperty(final String beacon, final String originKey) {
        return properties.get(creatKey(beacon, originKey));
    }
    public void setProperty(final String originKey, final String value, final String beacon) {
        if (value == null) {
            properties.remove(creatKey(beacon, originKey));
        } else {
            properties.put(creatKey(beacon, originKey), value);
        }
    }
    /**
     * @param beacon beacon ID.
     * @param originKey origin key
     * @return key with added beacon ID info
     */
    private String creatKey(final String beacon, final String originKey) {
        return beacon == null ? originKey : "bkn-" + beacon + "-" + originKey;
    }

    public Set<String> getKeys() {
        return new HashSet<>(properties.keySet());
    }
}
