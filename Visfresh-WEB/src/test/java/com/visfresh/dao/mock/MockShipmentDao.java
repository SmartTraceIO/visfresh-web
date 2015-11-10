/**
 *
 */
package com.visfresh.dao.mock;

import org.springframework.stereotype.Component;

import com.visfresh.controllers.ShipmentConstants;
import com.visfresh.dao.ShipmentDao;
import com.visfresh.entities.NotificationSchedule;
import com.visfresh.entities.Shipment;
import com.visfresh.entities.ShipmentStatus;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class MockShipmentDao extends MockEntityWithCompanyDaoBase<Shipment, Long> implements ShipmentDao {
    /**
     * Default constructor.
     */
    public MockShipmentDao() {
        super();
    }

    /* (non-Javadoc)
     * @see com.visfresh.dao.mock.MockDaoBase#save(com.visfresh.entities.EntityWithId)
     */
    @Override
    public <S extends Shipment> S save(final S entity) {
        final S e = super.save(entity);
        for (final NotificationSchedule n : e.getAlertsNotificationSchedules()) {
            if (n.getId() == null) {
                n.setId(generateId());
            }
        }
        return e;
    }
    /* (non-Javadoc)
     * @see com.visfresh.dao.ShipmentDao#findActiveShipment(java.lang.String)
     */
    @Override
    public Shipment findActiveShipment(final String imei) {
        for (final Shipment s : entities.values()) {
            if (s.getDevice().getImei().equals(imei) && s.getStatus() != ShipmentStatus.Complete) {
                return s;
            }
        }
        return null;
    }
    /* (non-Javadoc)
     * @see com.visfresh.dao.mock.MockDaoBase#getValueForFilterOrCompare(java.lang.String, com.visfresh.entities.EntityWithId)
     */
    @Override
    protected Object getValueForFilterOrCompare(final String property, final Shipment t) {
        if (property.equals(ShipmentConstants.PROPERTY_ALERT_PROFILE_ID)) {
            return t.getAlertProfile() == null ? null : t.getAlertProfile().getId();
        }
        if (property.equals(ShipmentConstants.PROPERTY_ALERT_PROFILE_NAME)) {
            return t.getAlertProfile() == null ? null : t.getAlertProfile().getName();
        }
        if (property.equals(ShipmentConstants.PROPERTY_ALERT_SUPPRESSION_MINUTES)) {
            return t.getAlertSuppressionMinutes();
        }
        if (property.equals(ShipmentConstants.PROPERTY_ARRIVAL_NOTIFICATION_WITHIN_KM)) {
            return t.getArrivalNotificationWithinKm();
        }
        if (property.equals(ShipmentConstants.PROPERTY_EXCLUDE_NOTIFICATIONS_IF_NO_ALERTS)) {
            return t.isExcludeNotificationsIfNoAlerts();
        }
        if (property.equals(ShipmentConstants.PROPERTY_SHIPPED_FROM)) {
            return t.getShippedFrom();
        }
        if (property.equals(ShipmentConstants.PROPERTY_SHIPPED_TO)) {
            return t.getShippedTo();
        }
        if (property.equals(ShipmentConstants.PROPERTY_SHUTDOWN_DEVICE_AFTER_MINUTES)) {
            return t.getShutdownDeviceTimeOut();
        }
        if (property.equals(ShipmentConstants.PROPERTY_MAX_TIMES_ALERT_FIRES)) {
            return t.getMaxTimesAlertFires();
        }
        if (property.equals(ShipmentConstants.PROPERTY_COMMENTS_FOR_RECEIVER)) {
            return t.getCommentsForReceiver();
        }
        if (property.equals(ShipmentConstants.PROPERTY_SHIPMENT_DESCRIPTION)) {
            return t.getShipmentDescription();
        }
        if (property.equals(ShipmentConstants.PROPERTY_ALERT_PROFILE)) {
            return t.getAlertProfile();
        }
        if (property.equals(ShipmentConstants.PROPERTY_SHIPPED_TO_LOCATION_NAME)) {
            return t.getShippedTo() == null ? null : t.getShippedTo().getName();
        }
        if (property.equals(ShipmentConstants.PROPERTY_SHIPPED_FROM_LOCATION_NAME)) {
            return t.getShippedFrom() == null ? null : t.getShippedFrom().getName();
        }
        if (property.equals(ShipmentConstants.PROPERTY_DEVICE_IMEI)) {
            return t.getDevice().getImei();
        }
        if (property.equals(ShipmentConstants.PROPERTY_STATUS)) {
            return t.getStatus();
        }
        if (property.equals(ShipmentConstants.PROPERTY_CUSTOM_FIELDS)) {
            return t.getCustomFields();
        }
        if (property.equals(ShipmentConstants.PROPERTY_SHIPMENT_DATE)) {
            return t.getShipmentDate();
        }
        if (property.equals(ShipmentConstants.PROPERTY_PO_NUM)) {
            return t.getPoNum();
        }
        if (property.equals(ShipmentConstants.PROPERTY_TRIP_COUNT)) {
            return t.getTripCount();
        }
        if (property.equals(ShipmentConstants.PROPERTY_ASSET_NUM)) {
            return t.getAssetNum();
        }
        if (property.equals(ShipmentConstants.PROPERTY_PALLET_ID)) {
            return t.getPalletId();
        }
        if (property.equals(ShipmentConstants.PROPERTY_SHIPMENT_ID)) {
            return t.getId();
        }
        if (property.equals(ShipmentConstants.PROPERTY_ASSET_TYPE)) {
            return t.getAssetType();
        }

        throw new IllegalStateException("Unaupported property: " + property);
    }
}
