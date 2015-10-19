/**
 *
 */
package com.visfresh;

import java.util.LinkedList;
import java.util.List;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class DeviceMessage extends DeviceMessageBase {
    /**
     * List of station signals.
     */
    private final List<StationSignal> stations = new LinkedList<StationSignal>();

    /**
     * Default constructor.
     */
    public DeviceMessage() {
        super();
    }

    /**
     * @return the stations
     */
    public List<StationSignal> getStations() {
        return stations;
    }
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder(super.toString()).append('\n');
        for (final StationSignal station : getStations()) {
            sb.append(station).append('\n');
        }
        return sb.toString();
    }
}
