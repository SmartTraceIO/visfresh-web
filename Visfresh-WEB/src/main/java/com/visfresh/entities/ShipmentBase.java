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
     * Send arrival report at the arrival.
     */
    private boolean sendArrivalReport = true;
    /**
     * Send arrival report only if there is alerts.
     */
    private boolean sendArrivalReportOnlyIfAlerts = false;

    /**
     * Shutdown device time out in minutes.
     */
    private Integer shutdownDeviceAfterMinutes;
    /**
     * Not alerts after arrival time out in minutes.
     */
    private Integer noAlertsAfterArrivalMinutes;
    /**
     * Shutdown after start minutes.
     */
    private Integer shutDownAfterStartMinutes;

    /**
     * Comments for receiver
     */
    private String commentsForReceiver;
    private Integer noAlertsAfterStartMinutes;
    /**
     * Is autostart template flag.
     */
    private boolean isAutostart;

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
        setShutdownDeviceAfterMinutes(shipment.getShutdownDeviceAfterMinutes());
        setCommentsForReceiver(shipment.getCommentsForReceiver());
        setSendArrivalReport(shipment.isSendArrivalReport());
        setSendArrivalReportOnlyIfAlerts(shipment.isSendArrivalReportOnlyIfAlerts());
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
     * @param b the excludeNotificationsIfNoAlertsFired to set
     */
    public void setExcludeNotificationsIfNoAlerts(final Boolean b) {
        this.excludeNotificationsIfNoAlerts = Boolean.TRUE.equals(b);
    }

    /**
     * @return the shutdownDevice
     */
    public Integer getShutdownDeviceAfterMinutes() {
        return shutdownDeviceAfterMinutes;
    }

    /**
     * @param minutes the shutdownDevice to set
     */
    public void setShutdownDeviceAfterMinutes(final Integer minutes) {
        this.shutdownDeviceAfterMinutes = minutes;
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
    /**
     * @return the noAlertsAfterArrivalMinutes
     */
    public Integer getNoAlertsAfterArrivalMinutes() {
        return noAlertsAfterArrivalMinutes;
    }
    /**
     * @param noAlertsAfterArrivalMinutes the noAlertsAfterArrivalMinutes to set
     */
    public void setNoAlertsAfterArrivalMinutes(final Integer noAlertsAfterArrivalMinutes) {
        this.noAlertsAfterArrivalMinutes = noAlertsAfterArrivalMinutes;
    }
    /**
     * @param minutes
     */
    public void setNoAlertsAfterStartMinutes(final Integer minutes) {
        this.noAlertsAfterStartMinutes = minutes;
    }
    /**
     * @return the noAlertsAfterStartMinutes
     */
    public Integer getNoAlertsAfterStartMinutes() {
        return noAlertsAfterStartMinutes;
    }
    /**
     * @return the shutDownAfterStartMinutes
     */
    public Integer getShutDownAfterStartMinutes() {
        return shutDownAfterStartMinutes;
    }
    /**
     * @param shutDownAfterStartMinutes the shutDownAfterStartMinutes to set
     */
    public void setShutDownAfterStartMinutes(final Integer shutDownAfterStartMinutes) {
        this.shutDownAfterStartMinutes = shutDownAfterStartMinutes;
    }
    /**
     * @return the isAutostart
     */
    public boolean isAutostart() {
        return isAutostart;
    }
    /**
     * @param isAutostart the isAutostart to set
     */
    public void setAutostart(final boolean isAutostart) {
        this.isAutostart = isAutostart;
    }

    /**
     * @return the sendArrivalReport
     */
    public boolean isSendArrivalReport() {
        return sendArrivalReport;
    }
    /**
     * @param sendArrivalReport the sendArrivalReport to set
     */
    public void setSendArrivalReport(final boolean sendArrivalReport) {
        this.sendArrivalReport = sendArrivalReport;
    }
    /**
     * @return the sendArrivalReportOnlyIfAlerts
     */
    public boolean isSendArrivalReportOnlyIfAlerts() {
        return sendArrivalReportOnlyIfAlerts;
    }
    /**
     * @param sendArrivalReportOnlyIfAlerts the sendArrivalReportOnlyIfAlerts to set
     */
    public void setSendArrivalReportOnlyIfAlerts(final boolean sendArrivalReportOnlyIfAlerts) {
        this.sendArrivalReportOnlyIfAlerts = sendArrivalReportOnlyIfAlerts;
    }
}
