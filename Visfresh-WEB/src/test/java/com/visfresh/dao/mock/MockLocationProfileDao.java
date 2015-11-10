/**
 *
 */
package com.visfresh.dao.mock;

import org.springframework.stereotype.Component;

import com.visfresh.controllers.LocationConstants;
import com.visfresh.dao.LocationProfileDao;
import com.visfresh.entities.LocationProfile;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class MockLocationProfileDao extends MockEntityWithCompanyDaoBase<LocationProfile, Long>
        implements LocationProfileDao {
    /**
     * Default constructor.
     */
    public MockLocationProfileDao() {
        super();
    }

    /* (non-Javadoc)
     * @see com.visfresh.dao.mock.MockDaoBase#getValueForFilterOrCompare(java.lang.String, com.visfresh.entities.EntityWithId)
     */
    @Override
    protected Object getValueForFilterOrCompare(final String property,
            final LocationProfile t) {
        if (property.equals(LocationConstants.PROPERTY_END_FLAG)) {
            return t.isStop();
        }
        if (property.equals(LocationConstants.PROPERTY_INTERIM_FLAG)) {
            return t.isInterim();
        }
        if (property.equals(LocationConstants.PROPERTY_START_FLAG)) {
            return t.isStart();
        }
        if (property.equals(LocationConstants.PROPERTY_RADIUS_METERS)) {
            return t.getRadius();
        }
        if (property.equals(LocationConstants.PROPERTY_LON)) {
            return t.getLocation().getLongitude();
        }
        if (property.equals(LocationConstants.PROPERTY_LAT)) {
            return t.getLocation().getLatitude();
        }
        if (property.equals(LocationConstants.PROPERTY_LOCATION)) {
            return t.getLocation();
        }
        if (property.equals(LocationConstants.PROPERTY_ADDRESS)) {
            return t.getAddress();
        }
        if (property.equals(LocationConstants.PROPERTY_NOTES)) {
            return t.getNotes();
        }
        if (property.equals(LocationConstants.PROPERTY_COMPANY_NAME)) {
            return t.getCompanyName();
        }
        if (property.equals(LocationConstants.PROPERTY_LOCATION_NAME)) {
            return t.getName();
        }
        if (property.equals(LocationConstants.PROPERTY_LOCATION_ID)) {
            return t.getId();
        }

        return null;
    }
}
