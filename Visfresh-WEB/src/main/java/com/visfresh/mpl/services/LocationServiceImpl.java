/**
 *
 */
package com.visfresh.mpl.services;

import org.springframework.stereotype.Component;

import com.visfresh.entities.Location;
import com.visfresh.services.LocationService;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class LocationServiceImpl implements LocationService {
    /**
     * Default constructor.
     */
    public LocationServiceImpl() {
        super();
    }

    /* (non-Javadoc)
     * @see com.visfresh.services.LocationService#getLocationDescription(com.visfresh.entities.Location)
     */
    @Override
    public String getLocationDescription(final Location loc) {
        //TODO implement
        return "Bankstown Warehouse";
    }
}
