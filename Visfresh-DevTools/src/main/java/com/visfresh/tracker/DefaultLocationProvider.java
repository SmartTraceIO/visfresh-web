/**
 *
 */
package com.visfresh.tracker;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class DefaultLocationProvider implements LocationProvider {
    private final Map<String, Location> locations = new HashMap<>();

    /**
     * Default constructor.
     */
    public DefaultLocationProvider() {
        super();
    }

    /* (non-Javadoc)
     * @see com.visfresh.tracker.LocationProvider#getLocation(java.util.List)
     */
    @Override
    public Location getLocation(String device, final List<StationSignal> signals) {
        return locations.get(createKey(signals));
    }
    public void addLocation(final List<StationSignal> signals, final Location location) {
        this.locations.put(createKey(signals), location);
    }

    /**
     * @param signals
     * @return
     */
    private String createKey(final List<StationSignal> signals) {
        final StringBuilder sb = new StringBuilder();
        for (final StationSignal s : signals) {
            if (sb.length() > 0) {
                //add separator
                sb.append("-*-");
            }
            sb.append(s.toString());
        }

        return sb.toString();
    }
}
