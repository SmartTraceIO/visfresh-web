/**
 *
 */
package com.visfresh.mpl.services;

import javax.annotation.PostConstruct;

import org.springframework.stereotype.Component;

import com.visfresh.entities.Device;
import com.visfresh.entities.TrackerEvent;
import com.visfresh.entities.User;
import com.visfresh.services.OpenJtsFacade;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class DefaultOpenJtsFacade implements OpenJtsFacade {

    /**
     * Default constructor.
     */
    public DefaultOpenJtsFacade() {
        super();
    }

    @PostConstruct
    public void initialize() {
        //Initialize Open JTS
    }

    /* (non-Javadoc)
     * @see com.visfresh.services.OpenJtsFacade#addUser(com.visfresh.entities.User)
     */
    @Override
    public void addUser(final User user) {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see com.visfresh.services.OpenJtsFacade#addDevice(com.visfresh.entities.Device)
     */
    @Override
    public void addDevice(final Device d) {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see com.visfresh.services.OpenJtsFacade#addTrackerEvent(com.visfresh.entities.TrackerEvent)
     */
    @Override
    public void addTrackerEvent(final TrackerEvent e) {
        // TODO Auto-generated method stub

    }

}
