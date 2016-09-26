/**
 *
 */
package com.visfresh.io;

import static com.visfresh.utils.EntityUtils.getEntityId;
import static com.visfresh.utils.EntityUtils.getIdList;

import java.util.LinkedList;
import java.util.List;

import com.visfresh.entities.ShipmentBase;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class ShipmentBaseDto {
    /**
     * ID.
     */
    private Long id;
    /**
     * Shipment description.
     */
    private String shipmentDescription;
    /**
     * Shipped from location.
     */
    private Long shippedFrom;
    /**
     * Shipped to location.
     */
    private Long shippedTo;
    /**
     * Alert profile
     */
    private Long alertProfile;
    /**
     * Alert notification schedules.
     */
    private final List<Long> alertsNotificationSchedules = new LinkedList<Long>();
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
    private final List<Long> arrivalNotificationSchedules = new LinkedList<Long>();
    /**
     * Exclude notifications if not alerts fired.
     */
    private boolean excludeNotificationsIfNoAlerts;
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
    private List<Long> interimLocations;
    private List<Long> interimStops;

    /**
     * Default constructor.
     */
    public ShipmentBaseDto() {
        super();
    }
    /**
     * @param tpl template.
     */
    public ShipmentBaseDto(final ShipmentBase tpl) {
        super();
        setId(tpl.getId());
        setShipmentDescription(tpl.getShipmentDescription());
        setShippedFrom(getEntityId(tpl.getShippedFrom()));
        setShippedTo(getEntityId(tpl.getShippedTo()));
        setAlertProfile(getEntityId(tpl.getAlertProfile()));
        setAlertSuppressionMinutes(tpl.getAlertSuppressionMinutes());
        getAlertsNotificationSchedules().addAll(getIdList(tpl.getAlertsNotificationSchedules()));
        setCommentsForReceiver(tpl.getCommentsForReceiver());
        setArrivalNotificationWithinKm(tpl.getArrivalNotificationWithinKm());
        setExcludeNotificationsIfNoAlerts(tpl.isExcludeNotificationsIfNoAlerts());
        getArrivalNotificationSchedules().addAll(getIdList(tpl.getArrivalNotificationSchedules()));
        setShutdownDeviceAfterMinutes(tpl.getShutdownDeviceAfterMinutes());
        setNoAlertsAfterArrivalMinutes(tpl.getNoAlertsAfterArrivalMinutes());
        setNoAlertsAfterStartMinutes(tpl.getNoAlertsAfterStartMinutes());
        setShutDownAfterStartMinutes(tpl.getShutDownAfterStartMinutes());
    }
    /**
     * @return the id
     */
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
    public Long getShippedFrom() {
        return shippedFrom;
    }
    /**
     * @param shippedFrom the shippedFrom to set
     */
    public void setShippedFrom(final Long shippedFrom) {
        this.shippedFrom = shippedFrom;
    }
    /**
     * @return the shippedTo
     */
    public Long getShippedTo() {
        return shippedTo;
    }
    /**
     * @param shippedTo the shippedTo to set
     */
    public void setShippedTo(final Long shippedTo) {
        this.shippedTo = shippedTo;
    }
    /**
     * @return the alertProfile
     */
    public Long getAlertProfile() {
        return alertProfile;
    }
    /**
     * @param alertProfile the alertProfile to set
     */
    public void setAlertProfile(final Long alertProfile) {
        this.alertProfile = alertProfile;
    }
    /**
     * @return the alertSuppressionMinutes
     */
    public int getAlertSuppressionMinutes() {
        return alertSuppressionMinutes;
    }
    /**
     * @param alertSuppressionMinutes the alertSuppressionMinutes to set
     */
    public void setAlertSuppressionMinutes(final int alertSuppressionMinutes) {
        this.alertSuppressionMinutes = alertSuppressionMinutes;
    }
    /**
     * @return the arrivalNotificationWithinKm
     */
    public Integer getArrivalNotificationWithinKm() {
        return arrivalNotificationWithinKm;
    }
    /**
     * @param arrivalNotificationWithinKm the arrivalNotificationWithinKm to set
     */
    public void setArrivalNotificationWithinKm(final Integer arrivalNotificationWithinKm) {
        this.arrivalNotificationWithinKm = arrivalNotificationWithinKm;
    }
    /**
     * @return the excludeNotificationsIfNoAlerts
     */
    public boolean isExcludeNotificationsIfNoAlerts() {
        return excludeNotificationsIfNoAlerts;
    }
    /**
     * @param excludeNotificationsIfNoAlerts the excludeNotificationsIfNoAlerts to set
     */
    public void setExcludeNotificationsIfNoAlerts(
            final boolean excludeNotificationsIfNoAlerts) {
        this.excludeNotificationsIfNoAlerts = excludeNotificationsIfNoAlerts;
    }
    /**
     * @return the shutdownDeviceAfterMinutes
     */
    public Integer getShutdownDeviceAfterMinutes() {
        return shutdownDeviceAfterMinutes;
    }
    /**
     * @param shutdownDeviceAfterMinutes the shutdownDeviceAfterMinutes to set
     */
    public void setShutdownDeviceAfterMinutes(final Integer shutdownDeviceAfterMinutes) {
        this.shutdownDeviceAfterMinutes = shutdownDeviceAfterMinutes;
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
     * @return the commentsForReceiver
     */
    public String getCommentsForReceiver() {
        return commentsForReceiver;
    }
    /**
     * @param commentsForReceiver the commentsForReceiver to set
     */
    public void setCommentsForReceiver(final String commentsForReceiver) {
        this.commentsForReceiver = commentsForReceiver;
    }
    /**
     * @return the noAlertsAfterStartMinutes
     */
    public Integer getNoAlertsAfterStartMinutes() {
        return noAlertsAfterStartMinutes;
    }
    /**
     * @param noAlertsAfterStartMinutes the noAlertsAfterStartMinutes to set
     */
    public void setNoAlertsAfterStartMinutes(final Integer noAlertsAfterStartMinutes) {
        this.noAlertsAfterStartMinutes = noAlertsAfterStartMinutes;
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
     * @return the alertsNotificationSchedules
     */
    public List<Long> getAlertsNotificationSchedules() {
        return alertsNotificationSchedules;
    }
    /**
     * @return the arrivalNotificationSchedules
     */
    public List<Long> getArrivalNotificationSchedules() {
        return arrivalNotificationSchedules;
    }
    /**
     * @param locations
     */
    public void setInterimLocations(final List<Long> locations) {
        this.interimLocations = locations;
    }
    /**
     * @return the interimLocations
     */
    public List<Long> getInterimLocations() {
        return interimLocations;
    }
    /**
     * @return the interimStops
     */
    public List<Long> getInterimStops() {
        return interimStops;
    }
    /**
     * @param interimStops the interimStops to set
     */
    public void setInterimStops(final List<Long> interimStops) {
        this.interimStops = interimStops;
    }
}
