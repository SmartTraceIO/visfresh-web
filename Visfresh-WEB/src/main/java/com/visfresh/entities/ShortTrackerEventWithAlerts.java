/**
 *
 */
package com.visfresh.entities;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class ShortTrackerEventWithAlerts extends ShortTrackerEvent {
    private static final long serialVersionUID = -38149942157383743L;

    private final Set<AlertType> alerts = new HashSet<>();

    /**
     * Default constructor.
     */
    public ShortTrackerEventWithAlerts() {
        super();
    }
    /**
     * @param e tracker event.
     */
    public ShortTrackerEventWithAlerts(final TrackerEvent e) {
        super(e);
    }
    /**
     * @param e
     */
    public ShortTrackerEventWithAlerts(final ShortTrackerEvent e) {
        super(e);
    }
    /**
     * @return the alerts
     */
    public Set<AlertType> getAlerts() {
        return alerts;
    }
}
