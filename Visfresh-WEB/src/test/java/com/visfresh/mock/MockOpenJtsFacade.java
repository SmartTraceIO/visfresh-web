/**
 *
 */
package com.visfresh.mock;

import org.springframework.stereotype.Component;

import com.visfresh.entities.Shipment;
import com.visfresh.entities.TrackerEvent;
import com.visfresh.entities.User;
import com.visfresh.services.OpenJtsFacade;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class MockOpenJtsFacade implements OpenJtsFacade {
    /**
     * Default constructor.
     */
    public MockOpenJtsFacade() {
        super();
    }
    /* (non-Javadoc)
     * @see com.visfresh.services.OpenJtsFacade#addUser(com.visfresh.entities.User, java.lang.String)
     */
    @Override
    public void addUser(final User user, final String password) {
    }
    /* (non-Javadoc)
     * @see com.visfresh.services.OpenJtsFacade#addTrackerEvent(com.visfresh.entities.Shipment, com.visfresh.entities.TrackerEvent)
     */
    @Override
    public void addTrackerEvent(final Shipment shipment, final TrackerEvent e) {
    }
}
