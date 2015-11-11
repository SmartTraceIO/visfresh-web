/**
 *
 */
package com.visfresh.rules;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class DeviceState {
    private final Map<String, Date> dates = new HashMap<String, Date>();

    /**
     * Default constructor.
     */
    public DeviceState() {
        super();
    }

    /**
     * Flushes temperature savings.
     */
    public void flushDates() {
        dates.clear();
    }

    /**
     * @param key key date.
     * @param date date.
     */
    public void setDate(final String key, final Date date) {
        dates.put(key, date);
    }

    /**
     * @param key
     * @return
     */
    public Date getDate(final String key) {
        return dates.get(key);
    }
    /**
     * @return the dates
     */
    public Map<String, Date> getDates() {
        return dates;
    }
}
