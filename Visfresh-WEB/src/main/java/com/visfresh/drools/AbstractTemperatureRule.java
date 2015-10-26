/**
 *
 */
package com.visfresh.drools;

import org.springframework.stereotype.Component;

import com.visfresh.entities.TrackerEvent;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public abstract class AbstractTemperatureRule implements TrackerEventRule {
    /**
     * Default constructor.
     */
    public AbstractTemperatureRule() {
        super();
    }

    /* (non-Javadoc)
     * @see com.visfresh.drools.TrackerEventRule#accept(com.visfresh.drools.TrackerEventRequest)
     */
    @Override
    public boolean accept(final TrackerEventRequest e) {
        final TrackerEvent event = e.getEvent();
        return e.getClientProperty(this) == null
                && event.getShipment() != null
                && event.getShipment().getAlertProfile() != null;
    }

    /* (non-Javadoc)
     * @see com.visfresh.drools.TrackerEventRule#handle(com.visfresh.drools.TrackerEventRequest)
     */
    @Override
    public final boolean handle(final TrackerEventRequest e) {
        handleInternal(e.getEvent());
        e.putClientProperty(this, Boolean.TRUE);
        return false;
    }

    /**
     * @param event event.
     */
    protected abstract void handleInternal(TrackerEvent event);
}
