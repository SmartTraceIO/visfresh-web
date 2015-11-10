/**
 *
 */
package com.visfresh.dao.mock;

import org.springframework.stereotype.Component;

import com.visfresh.controllers.AlertProfileConstants;
import com.visfresh.dao.AlertProfileDao;
import com.visfresh.entities.AlertProfile;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class MockAlertProfileDao extends MockEntityWithCompanyDaoBase<AlertProfile, Long> implements AlertProfileDao {
    /**
     * Default constructor.
     */
    public MockAlertProfileDao() {
        super();
    }

    /* (non-Javadoc)
     * @see com.visfresh.dao.mock.MockEntityWithCompanyDaoBase#getValue(java.lang.String, com.visfresh.entities.EntityWithId)
     */
    @Override
    protected Object getValueForFilterOrCompare(final String property, final AlertProfile t) {
        if (property.equals(AlertProfileConstants.PROPERTY_WATCH_MOVEMENT_STOP)) {
            return t.isWatchMovementStop();
        } else if (property.equals(AlertProfileConstants.PROPERTY_WATCH_MOVEMENT_START)) {
            return t.isWatchMovementStart();
        } else if (property.equals(AlertProfileConstants.PROPERTY_WATCH_ENTER_DARK_ENVIRONMENT)) {
            return t.isWatchEnterDarkEnvironment();
        } else if (property.equals(AlertProfileConstants.PROPERTY_WATCH_ENTER_BRIGHT_ENVIRONMENT)) {
            return t.isWatchEnterBrightEnvironment();
        } else if (property.equals(AlertProfileConstants.PROPERTY_WATCH_BATTERY_LOW)) {
            return t.isWatchBatteryLow();
        } else if (property.equals(AlertProfileConstants.PROPERTY_CRITICAL_LOW_TEMPERATURE_MINUTES2)) {
            return t.getCriticalHighTemperatureForMoreThen2();
        } else if (property.equals(AlertProfileConstants.PROPERTY_CRITICAL_LOW_TEMPERATURE2)) {
            return t.getCriticalLowTemperature2();
        } else if (property.equals(AlertProfileConstants.PROPERTY_CRITICAL_LOW_TEMPERATURE_MINUTES)) {
            return t.getCriticalLowTemperatureForMoreThen();
        } else if (property.equals(AlertProfileConstants.PROPERTY_CRITICAL_LOW_TEMPERATURE)) {
            return t.getCriticalLowTemperature();
        } else if (property.equals(AlertProfileConstants.PROPERTY_LOW_TEMPERATURE_MINUTES2)) {
            return t.getLowTemperatureForMoreThen2();
        } else if (property.equals(AlertProfileConstants.PROPERTY_LOW_TEMPERATURE2)) {
            return t.getLowTemperature2();
        } else if (property.equals(AlertProfileConstants.PROPERTY_LOW_TEMPERATURE_MINUTES)) {
            return t.getLowTemperatureForMoreThen();
        } else if (property.equals(AlertProfileConstants.PROPERTY_LOW_TEMPERATURE)) {
            return t.getLowTemperature();
        } else if (property.equals(AlertProfileConstants.PROPERTY_CRITICAL_HIGH_TEMPERATURE_MINUTES2)) {
            return t.getCriticalHighTemperatureForMoreThen2();
        } else if (property.equals(AlertProfileConstants.PROPERTY_CRITICAL_HIGH_TEMPERATURE2)) {
            return t.getCriticalHighTemperature2();
        } else if (property.equals(AlertProfileConstants.PROPERTY_CRITICAL_HIGH_TEMPERATURE_MINUTES)) {
            return t.getCriticalHighTemperatureForMoreThen();
        } else if (property.equals(AlertProfileConstants.PROPERTY_CRITICAL_HIGH_TEMPERATURE)) {
            return t.getCriticalHighTemperature();
        } else if (property.equals(AlertProfileConstants.PROPERTY_HIGH_TEMPERATURE_MINUTES2)) {
            return t.getHighTemperatureForMoreThen2();
        } else if (property.equals(AlertProfileConstants.PROPERTY_HIGH_TEMPERATURE2)) {
            return t.getHighTemperature2();
        } else if (property.equals(AlertProfileConstants.PROPERTY_HIGH_TEMPERATURE_MINUTES)) {
            return t.getHighTemperatureForMoreThen();
        } else if (property.equals(AlertProfileConstants.PROPERTY_HIGH_TEMPERATURE)) {
            return t.getHighTemperature();
        } else if (property.equals(AlertProfileConstants.PROPERTY_ALERT_PROFILE_DESCRIPTION)) {
            return t.getDescription();
        } else if (property.equals(AlertProfileConstants.PROPERTY_ALERT_PROFILE_NAME)) {
            return t.getName();
        } else if (property.equals(AlertProfileConstants.PROPERTY_ALERT_PROFILE_ID)) {
            return t.getId();
        }

        throw new IllegalArgumentException("Undefined Property: " + property);
    }
}
