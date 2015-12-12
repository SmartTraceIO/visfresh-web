/**
 *
 */
package com.visfresh.mock;

import org.springframework.stereotype.Component;

import com.visfresh.entities.Location;
import com.visfresh.services.LocationService;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class MockLocationService implements LocationService {

    /**
     * Default constructor.
     */
    public MockLocationService() {
        super();
    }

    /* (non-Javadoc)
     * @see com.visfresh.services.LocationService#getLocationDescription(com.visfresh.entities.Location)
     */
    @Override
    public String getLocationDescription(final Location loc) {
        return "Bankstown Warehouse";
    }
}
