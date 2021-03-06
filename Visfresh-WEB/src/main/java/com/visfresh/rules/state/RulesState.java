/**
 *
 */
package com.visfresh.rules.state;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The class for persists the current rules properties. The key goal is the properties map.
 * So, dates are stored as different map for better usability.
 *
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class RulesState {
    private final Map<String, Date> dates = new ConcurrentHashMap<String, Date>();
    private final Map<String, String> properties = new ConcurrentHashMap<String, String>();

    /**
     * Default consructor.
     */
    public RulesState() {
        super();
    }

    /**
     * @return the dates
     */
    public Map<String, Date> getDates() {
        return dates;
    }
    /**
     * @return the properties
     */
    public Map<String, String> getProperties() {
        return properties;
    }
    /**
     * Clears all properties of state.
     */
    public void clear() {
        dates.clear();
        properties.clear();
    }
}
