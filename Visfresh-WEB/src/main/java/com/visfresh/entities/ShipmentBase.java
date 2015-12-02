/**
 *
 */
package com.visfresh.entities;

import java.util.LinkedList;
import java.util.List;

/**
 * Base class for Shipment templates and Shipments
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public abstract class ShipmentBase implements EntityWithId<Long>, EntityWithCompany {
    /**
     * ID.
     */
    private Long id;
    /**
     * Company name.
     */
    private Company company;
    /**
     * Shipment description.
     */
    private String shipmentDescription;
    /**
     * Shipped from location.
     */
    private LocationProfile shippedFrom;
    /**
     * Shipped to location.
     */
    private LocationProfile shippedTo;
    /**
     * Alert profile
     */
    private AlertProfile alertProfile;
    /**
     * Alert notification schedules.
     */
    private final List<NotificationSchedule> alertsNotificationSchedules = new LinkedList<NotificationSchedule>();
    /**
     * Alert suppression time in hours.
     */
    private int alertSuppressionMinutes;
    /**
     * Arrival notification with in.
     */
    private Integer arrivalNotificationWithinKm;
    /**
     * Arrival notification shedules.
     */
    private final List<NotificationSchedule> arrivalNotificationSchedules = new LinkedList<NotificationSchedule>();
    /**
     * Exclude notifications if not alerts fired.
     */
    private boolean excludeNotificationsIfNoAlerts;
    /**
     * Shutdown device time out in minutes.
     */
    private Integer shutdownDeviceAfterMinutes;
    /**
     * Comments for receiver
     */
    private String commentsForReceiver;

    /**
     * Default constructor.
     */
    public ShipmentBase() {
        super();
    }

    /**
     * @param shipment the shipment to copy.
     */
    public ShipmentBase(final ShipmentBase shipment) {
        super();

        setShipmentDescription(shipment.getShipmentDescription());
        setShippedFrom(shipment.getShippedFrom());
        setShippedTo(shipment.getShippedTo());
        setAlertProfile(shipment.getAlertProfile());
        alertsNotificationSchedules.addAll((shipment.getAlertsNotificationSchedules()));
        setAlertSuppressionMinutes(shipment.getAlertSuppressionMinutes());
        setArrivalNotificationWithinKm(shipment.getArrivalNotificationWithinKm());
        arrivalNotificationSchedules.addAll((shipment.getArrivalNotificationSchedules()));
        setExcludeNotificationsIfNoAlerts(shipment.isExcludeNotificationsIfNoAlerts());
        setShutdownDeviceTimeOut(shipment.getShutdownDeviceTimeOut());
    }

    /**
     * @return the shipmentDescription
     */
    public String getShipmentDescription() {
        return shipmentDescription;
    }

    /**
     * @param shipmentDescription the shipmentDescription to set
     */
    public void setShipmentDescription(final String shipmentDescription) {
        this.shipmentDescription = shipmentDescription;
    }

    /**
     * @return the shippedFrom
     */
    public LocationProfile getShippedFrom() {
        return shippedFrom;
    }

    /**
     * @param shippedFrom the shippedFrom to set
     */
    public void setShippedFrom(final LocationProfile shippedFrom) {
        this.shippedFrom = shippedFrom;
    }

    /**
     * @return the shippedTo
     */
    public LocationProfile getShippedTo() {
        return shippedTo;
    }

    /**
     * @param shippedTo the shippedTo to set
     */
    public void setShippedTo(final LocationProfile shippedTo) {
        this.shippedTo = shippedTo;
    }

    /**
     * @return the alertProfile
     */
    public AlertProfile getAlertProfile() {
        return alertProfile;
    }

    /**
     * @param alertProfile the alertProfile to set
     */
    public void setAlertProfile(final AlertProfile alertProfile) {
        this.alertProfile = alertProfile;
    }

    /**
     * @return the alertsNotificationSchedule
     */
    public List<NotificationSchedule> getAlertsNotificationSchedules() {
        return alertsNotificationSchedules;
    }
    /**
     * @return the alertSuppressionDuringCoolDown
     */
    public int getAlertSuppressionMinutes() {
        return alertSuppressionMinutes;
    }

    /**
     * @param alertSuppressionDuringCoolDown the alertSuppressionDuringCoolDown to set
     */
    public void setAlertSuppressionMinutes(final int alertSuppressionDuringCoolDown) {
        this.alertSuppressionMinutes = alertSuppressionDuringCoolDown;
    }

    /**
     * @return the arrivalNotification
     */
    public Integer getArrivalNotificationWithinKm() {
        return arrivalNotificationWithinKm;
    }

    /**
     * @param km number of kilometers for arrival notification.
     */
    public void setArrivalNotificationWithinKm(final Integer km) {
        this.arrivalNotificationWithinKm = km;
    }

    /**
     * @return the arrivalNotificationSchedule
     */
    public List<NotificationSchedule> getArrivalNotificationSchedules() {
        return arrivalNotificationSchedules;
    }
    /**
     * @return the excludeNotificationsIfNoAlertsFired
     */
    public boolean isExcludeNotificationsIfNoAlerts() {
        return excludeNotificationsIfNoAlerts;
    }

    /**
     * @param excludeNotificationsIfNoAlertsFired the excludeNotificationsIfNoAlertsFired to set
     */
    public void setExcludeNotificationsIfNoAlerts(final boolean excludeNotificationsIfNoAlertsFired) {
        this.excludeNotificationsIfNoAlerts = excludeNotificationsIfNoAlertsFired;
    }

    /**
     * @return the shutdownDevice
     */
    public Integer getShutdownDeviceTimeOut() {
        return shutdownDeviceAfterMinutes;
    }

    /**
     * @param ninutes the shutdownDevice to set
     */
    public void setShutdownDeviceTimeOut(final Integer ninutes) {
        this.shutdownDeviceAfterMinutes = ninutes;
    }
    /**
     * @return the company
     */
    @Override
    public Company getCompany() {
        return company;
    }
    /**
     * @param company the company to set
     */
    @Override
    public void setCompany(final Company company) {
        this.company = company;
    }
    /**
     * @return the id
     */
    @Override
    public Long getId() {
        return id;
    }
    /**
     * @param id the id to set
     */
    public void setId(final Long id) {
        this.id = id;
    }
    /**
     * @return the commentsForReceiver
     */
    public String getCommentsForReceiver() {
        return commentsForReceiver;
    }
    /**
     * @param comments the commentsForReceiver to set
     */
    public void setCommentsForReceiver(final String comments) {
        this.commentsForReceiver = comments;
    }
}
