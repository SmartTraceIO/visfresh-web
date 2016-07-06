/**
 *
 */
package com.visfresh.io;

import java.util.LinkedList;
import java.util.List;

import com.visfresh.entities.AutoStartShipment;
import com.visfresh.entities.LocationProfile;
import com.visfresh.entities.NotificationSchedule;
import com.visfresh.entities.ShipmentTemplate;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class AutoStartShipmentDto {
    /**
     * The list of start location ID.
     */
    private final List<Long> startLocations = new LinkedList<>();
    private final List<String> startLocationNames = new LinkedList<>();
    /**
     * The list of end location ID.
     */
    private final List<Long> endLocations = new LinkedList<>();
    private final List<String> endLocationNames = new LinkedList<>();

    private final List<Long> interimStops = new LinkedList<>();
    private final List<String> interimStopsNames = new LinkedList<>();

    private int priority;
    private Long id;
    private String alertProfileName;

    /**
     * Name.
     */
    private String name;
    /**
     * Shipment description.
     */
    private String shipmentDescription;
    /**
     * Add data shipped.
     */
    private boolean addDateShipped;
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
    private boolean startOnLeaveLocation;

    /**
     * Default constructor.
     */
    public AutoStartShipmentDto() {
        super();
    }
    /**
     * @param cfg default shipment configuration.
     */
    public AutoStartShipmentDto(final AutoStartShipment cfg) {
        super();

        setId(cfg.getId());
        setPriority(cfg.getPriority());
        setStartOnLeaveLocation(cfg.isStartOnLeaveLocation());

        //add start locations.
        for (final LocationProfile loc : cfg.getShippedFrom()) {
            startLocations.add(loc.getId());
            startLocationNames.add(loc.getName());
        }
        //add end locations.
        for (final LocationProfile loc : cfg.getShippedTo()) {
            endLocations.add(loc.getId());
            endLocationNames.add(loc.getName());
        }
        //add interim stops.
        for (final LocationProfile loc : cfg.getInterimStops()) {
            interimStops.add(loc.getId());
            interimStopsNames.add(loc.getName());
        }

        //shipment template fields.
        final ShipmentTemplate tpl = cfg.getTemplate();

        setName(tpl.getName());
        setShipmentDescription(tpl.getShipmentDescription());
        setAddDateShipped(tpl.isAddDateShipped());
        if (tpl.getAlertProfile() != null) {
            setAlertProfile(tpl.getAlertProfile().getId());
            setAlertProfileName(tpl.getAlertProfile().getName());
        }
        for (final NotificationSchedule s : tpl.getAlertsNotificationSchedules()) {
            alertsNotificationSchedules.add(s.getId());
        }
        setAlertSuppressionMinutes(tpl.getAlertSuppressionMinutes());
        setArrivalNotificationWithinKm(tpl.getArrivalNotificationWithinKm());
        for (final NotificationSchedule s : tpl.getArrivalNotificationSchedules()) {
            arrivalNotificationSchedules.add(s.getId());
        }
        setExcludeNotificationsIfNoAlerts(tpl.isExcludeNotificationsIfNoAlerts());
        setShutdownDeviceAfterMinutes(tpl.getShutdownDeviceAfterMinutes());
        setNoAlertsAfterArrivalMinutes(tpl.getNoAlertsAfterArrivalMinutes());
        setNoAlertsAfterStartMinutes(tpl.getNoAlertsAfterStartMinutes());
        setShutDownAfterStartMinutes(tpl.getShutDownAfterStartMinutes());
        setCommentsForReceiver(tpl.getCommentsForReceiver());
    }
    /**
     * @return the list of locations.
     */
    public List<Long> getStartLocations() {
        return startLocations;
    }
    /**
     * @return list of end locations.
     */
    public List<Long> getEndLocations() {
        return endLocations;
    }
    /**
     * @param id default shipment config ID.
     */
    public void setId(final Long id) {
        this.id = id;
    }
    /**
     * @return the id
     */
    public Long getId() {
        return id;
    }
    /**
     * @param priority the priority to set
     */
    public void setPriority(final int priority) {
        this.priority = priority;
    }
    /**
     * @return the priority
     */
    public int getPriority() {
        return priority;
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
     * @return the addDateShipped
     */
    public boolean isAddDateShipped() {
        return addDateShipped;
    }
    /**
     * @param addDateShipped the addDateShipped to set
     */
    public void setAddDateShipped(final boolean addDateShipped) {
        this.addDateShipped = addDateShipped;
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
     * @return the alertProfileName
     */
    public String getAlertProfileName() {
        return alertProfileName;
    }
    /**
     * @param alertProfileName the alertProfileName to set
     */
    public void setAlertProfileName(final String alertProfileName) {
        this.alertProfileName = alertProfileName;
    }
    /**
     * @return the endLocationNames
     */
    public List<String> getEndLocationNames() {
        return endLocationNames;
    }
    /**
     * @return the startLocationNames
     */
    public List<String> getStartLocationNames() {
        return startLocationNames;
    }
    /**
     * @return
     */
    public List<Long> getInterimStops() {
        return interimStops;
    }
    /**
     * @return the startLocationNames
     */
    public List<String> getInterimStopsNames() {
        return interimStopsNames;
    }
    /**
     * @return the startOnLeaveLocation
     */
    public boolean isStartOnLeaveLocation() {
        return startOnLeaveLocation;
    }
    /**
     * @param startOnLeaveLocation the startOnLeaveLocation to set
     */
    public void setStartOnLeaveLocation(final boolean startOnLeaveLocation) {
        this.startOnLeaveLocation = startOnLeaveLocation;
    }
}
