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
public abstract class ShipmentBase implements EntityWithId<Long> {
    /**
     * ID.
     */
    private Long id;
    /**
     * Company name.
     */
    private Company company;
    /**
     * Name.
     */
    private String name;
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
    private int arrivalNotificationWithinKm;
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
    private int shutdownDeviceAfterMinutes;
    /**
     * Max count of alerts of given type.
     */
    private int maxTimesAlertFires = 4;

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

        setName(shipment.getName());
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
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(final String name) {
        this.name = name;
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
    public int getArrivalNotificationWithinKm() {
        return arrivalNotificationWithinKm;
    }

    /**
     * @param km number of kilometers for arrival notification.
     */
    public void setArrivalNotificationWithinKm(final int km) {
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
    public int getShutdownDeviceTimeOut() {
        return shutdownDeviceAfterMinutes;
    }

    /**
     * @param ninutes the shutdownDevice to set
     */
    public void setShutdownDeviceTimeOut(final int ninutes) {
        this.shutdownDeviceAfterMinutes = ninutes;
    }
    /**
     * @return the company
     */
    public Company getCompany() {
        return company;
    }
    /**
     * @param company the company to set
     */
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
     * @return the maxTimesAlertFires
     */
    public int getMaxTimesAlertFires() {
        return maxTimesAlertFires;
    }
    /**
     * @param maxTimesAlertFires the maxTimesAlertFires to set
     */
    public void setMaxTimesAlertFires(final int maxTimesAlertFires) {
        this.maxTimesAlertFires = maxTimesAlertFires;
    }
}
