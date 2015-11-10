/**
 *
 */
package com.visfresh.dao.mock;

import org.springframework.stereotype.Component;

import com.visfresh.controllers.ShipmentTemplateConstants;
import com.visfresh.dao.ShipmentTemplateDao;
import com.visfresh.entities.NotificationSchedule;
import com.visfresh.entities.ShipmentTemplate;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class MockShipmentTemplateDao extends MockEntityWithCompanyDaoBase<ShipmentTemplate, Long> implements ShipmentTemplateDao {
    /**
     * Default constructor.
     */
    public MockShipmentTemplateDao() {
        super();
    }

    /* (non-Javadoc)
     * @see com.visfresh.dao.mock.MockDaoBase#save(com.visfresh.entities.EntityWithId)
     */
    @Override
    public <S extends ShipmentTemplate> S save(final S entity) {
        final S e = super.save(entity);
        for (final NotificationSchedule n : e.getAlertsNotificationSchedules()) {
            if (n.getId() == null) {
                n.setId(generateId());
            }
        }
        return e;
    }
    /* (non-Javadoc)
     * @see com.visfresh.dao.mock.MockDaoBase#getValueForFilterOrCompare(java.lang.String, com.visfresh.entities.EntityWithId)
     */
    @Override
    protected Object getValueForFilterOrCompare(final String property, final ShipmentTemplate t) {
        if (property.equals(ShipmentTemplateConstants.PROPERTY_SHUTDOWN_DEVICE_AFTER_MINUTES)) {
            return t.getShutdownDeviceTimeOut();
        }
        if (property.equals(ShipmentTemplateConstants.PROPERTY_EXCLUDE_NOTIFICATIONS_IF_NO_ALERTS)) {
            return t.isExcludeNotificationsIfNoAlerts();
        }
        if (property.equals(ShipmentTemplateConstants.PROPERTY_ARRIVAL_NOTIFICATION_WITHIN_KM)) {
            return t.getArrivalNotificationWithinKm();
        }
        if (property.equals(ShipmentTemplateConstants.PROPERTY_COMMENTS_FOR_RECEIVER)) {
            return t.getCommentsForReceiver();
        }
        if (property.equals(ShipmentTemplateConstants.PROPERTY_MAX_TIMES_ALERT_FIRES)) {
            return t.getMaxTimesAlertFires();
        }
        if (property.equals(ShipmentTemplateConstants.PROPERTY_ALERT_SUPPRESSION_MINUTES)) {
            return t.getAlertSuppressionMinutes();
        }
        if (property.equals(ShipmentTemplateConstants.PROPERTY_USE_CURRENT_TIME_FOR_DATE_SHIPPED)) {
            return t.isUseCurrentTimeForDateShipped();
        }
        if (property.equals(ShipmentTemplateConstants.PROPERTY_DETECT_LOCATION_FOR_SHIPPED_FROM)) {
            return t.isDetectLocationForShippedFrom();
        }
        if (property.equals(ShipmentTemplateConstants.PROPERTY_SHIPPED_TO)) {
            return t.getShippedTo();
        }
        if (property.equals(ShipmentTemplateConstants.PROPERTY_SHIPPED_FROM)) {
            return t.getShippedFrom();
        }
        if (property.equals(ShipmentTemplateConstants.PROPERTY_ADD_DATE_SHIPPED)) {
            return t.isAddDateShipped();
        }
        if (property.equals(ShipmentTemplateConstants.PROPERTY_SHIPMENT_DESCRIPTION)) {
            return t.getShipmentDescription();
        }
        if (property.equals(ShipmentTemplateConstants.PROPERTY_SHIPMENT_TEMPLATE_NAME)) {
            return t.getName();
        }
        if (property.equals(ShipmentTemplateConstants.PROPERTY_SHIPMENT_TEMPLATE_ID)) {
            return t.getId();
        }
        if (property.equals(ShipmentTemplateConstants.PROPERTY_ALERT_PROFILE_ID)) {
            return t.getAlertProfile() == null ? null : t.getAlertProfile().getId();
        }

        throw new IllegalStateException("Unsupported property: " + property);
    }
}
