/**
 *
 */
package com.visfresh.io.shipment;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import com.visfresh.entities.AlertType;
import com.visfresh.entities.InterimStop;
import com.visfresh.entities.Location;
import com.visfresh.entities.LocationProfile;
import com.visfresh.entities.ShipmentStatus;
import com.visfresh.lists.ListNotificationScheduleItem;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class SingleShipmentDto {
    private long shipmentId;
    private String deviceSN;
    private String deviceName;
    private int tripCount;
    private String shipmentDescription;
    private String palletId;
    private String assetNum;
    private String assetType;
    private ShipmentStatus status;
    private Long alertProfileId;
    private Integer alertSuppressionMinutes;
    private final List<ListNotificationScheduleItem> alertsNotificationSchedules = new LinkedList<>();
    private String commentsForReceiver;
    private Integer arrivalNotificationWithinKm;
    boolean excludeNotificationsIfNoAlerts;
    private final List<ListNotificationScheduleItem> arrivalNotificationSchedules = new LinkedList<>();
    private Integer shutdownDeviceAfterMinutes;
    private Integer noAlertsAfterArrivalMinutes;
    private Integer shutDownAfterStartMinutes;
    private String startLocation;
    private String startTimeISO;
    private String startTime;
    private String endLocation;
    private String etaIso;
    private String eta;
    private String currentLocation;
    private Location startLocationForMap;
    private Location endLocationForMap;
    private Location currentLocationForMap;
    private int percentageComplete;

    private double minTemp;
    private double maxTemp;
    private String timeOfFirstReading;

    private final List<SingleShipmentLocation> locations = new LinkedList<>();
    private final List<SingleShipmentDto> siblings = new LinkedList<>();
    private String alertProfileName;
    private final Set<AlertType> alertSummary = new HashSet<>();
    private String alertYetToFire;
    private String arrivalNotificationTimeIso;
    private String arrivalNotificationTime;
    private String shutdownTimeIso;
    private String shutdownTime;
    private String arrivalTimeIso;
    private String arrivalTime;
    private String firstReadingTime;
    private String lastReadingTimeIso;
    private String lastReadingTime;
    private double lastReadingTemperature;
    private Integer noAlertsAfterStartMinutes;

    private final List<LocationProfile> startLocationAlternatives = new LinkedList<>();
    private final List<LocationProfile> endLocationAlternatives = new LinkedList<>();
    private final List<LocationProfile> interimLocationAlternatives = new LinkedList<>();
    private final List<InterimStop> interimStops = new LinkedList<>();

    /**
     * Default constructor.
     */
    public SingleShipmentDto() {
        super();
    }

    /**
     * @return the shipmentId
     */
    public long getShipmentId() {
        return shipmentId;
    }
    /**
     * @param shipmentId the shipmentId to set
     */
    public void setShipmentId(final long shipmentId) {
        this.shipmentId = shipmentId;
    }
    /**
     * @return the deviceSN
     */
    public String getDeviceSN() {
        return deviceSN;
    }
    /**
     * @param deviceSN the deviceSN to set
     */
    public void setDeviceSN(final String deviceSN) {
        this.deviceSN = deviceSN;
    }
    /**
     * @return the deviceName
     */
    public String getDeviceName() {
        return deviceName;
    }
    /**
     * @param deviceName the deviceName to set
     */
    public void setDeviceName(final String deviceName) {
        this.deviceName = deviceName;
    }
    /**
     * @return the tripCount
     */
    public int getTripCount() {
        return tripCount;
    }
    /**
     * @param tripCount the tripCount to set
     */
    public void setTripCount(final int tripCount) {
        this.tripCount = tripCount;
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
     * @return the palletId
     */
    public String getPalletId() {
        return palletId;
    }
    /**
     * @param palletId the palletId to set
     */
    public void setPalletId(final String palletId) {
        this.palletId = palletId;
    }
    /**
     * @return the assetNum
     */
    public String getAssetNum() {
        return assetNum;
    }
    /**
     * @param assetNum the assetNum to set
     */
    public void setAssetNum(final String assetNum) {
        this.assetNum = assetNum;
    }
    /**
     * @return the assetType
     */
    public String getAssetType() {
        return assetType;
    }
    /**
     * @param assetType the assetType to set
     */
    public void setAssetType(final String assetType) {
        this.assetType = assetType;
    }
    /**
     * @return the status
     */
    public ShipmentStatus getStatus() {
        return status;
    }
    /**
     * @param status the status to set
     */
    public void setStatus(final ShipmentStatus status) {
        this.status = status;
    }
    /**
     * @return the alertProfileId
     */
    public Long getAlertProfileId() {
        return alertProfileId;
    }
    /**
     * @param alertProfileId the alertProfileId to set
     */
    public void setAlertProfileId(final Long alertProfileId) {
        this.alertProfileId = alertProfileId;
    }
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
     * @return the alertSuppressionMinutes
     */
    public Integer getAlertSuppressionMinutes() {
        return alertSuppressionMinutes;
    }
    /**
     * @param alertSuppressionMinutes the alertSuppressionMinutes to set
     */
    public void setAlertSuppressionMinutes(final Integer alertSuppressionMinutes) {
        this.alertSuppressionMinutes = alertSuppressionMinutes;
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
     * @return the startLocation
     */
    public String getStartLocation() {
        return startLocation;
    }
    /**
     * @param startLocation the startLocation to set
     */
    public void setStartLocation(final String startLocation) {
        this.startLocation = startLocation;
    }
    /**
     * @return the startTimeISO
     */
    public String getStartTimeISO() {
        return startTimeISO;
    }
    /**
     * @param startTimeISO the startTimeISO to set
     */
    public void setStartTimeISO(final String startTimeISO) {
        this.startTimeISO = startTimeISO;
    }
    /**
     * @return the endLocation
     */
    public String getEndLocation() {
        return endLocation;
    }
    /**
     * @param endLocation the endLocation to set
     */
    public void setEndLocation(final String endLocation) {
        this.endLocation = endLocation;
    }
    /**
     * @return the eta
     */
    public String getEtaIso() {
        return etaIso;
    }
    /**
     * @param eta the eta to set
     */
    public void setEtaIso(final String eta) {
        this.etaIso = eta;
    }
    /**
     * @return the currentLocation
     */
    public String getCurrentLocation() {
        return currentLocation;
    }
    /**
     * @param currentLocation the currentLocation to set
     */
    public void setCurrentLocation(final String currentLocation) {
        this.currentLocation = currentLocation;
    }
    /**
     * @return the startLocationForMap
     */
    public Location getStartLocationForMap() {
        return startLocationForMap;
    }
    /**
     * @param startLocationForMap the startLocationForMap to set
     */
    public void setStartLocationForMap(final Location startLocationForMap) {
        this.startLocationForMap = startLocationForMap;
    }
    /**
     * @return the endLocationForMap
     */
    public Location getEndLocationForMap() {
        return endLocationForMap;
    }
    /**
     * @param endLocationForMap the endLocationForMap to set
     */
    public void setEndLocationForMap(final Location endLocationForMap) {
        this.endLocationForMap = endLocationForMap;
    }
    /**
     * @return the currentLocationForMap
     */
    public Location getCurrentLocationForMap() {
        return currentLocationForMap;
    }
    /**
     * @param currentLocationForMap the currentLocationForMap to set
     */
    public void setCurrentLocationForMap(final Location currentLocationForMap) {
        this.currentLocationForMap = currentLocationForMap;
    }
    /**
     * @return the alertsNotificationSchedules
     */
    public List<ListNotificationScheduleItem> getAlertsNotificationSchedules() {
        return alertsNotificationSchedules;
    }
    /**
     * @return the arrivalNotificationSchedules
     */
    public List<ListNotificationScheduleItem> getArrivalNotificationSchedules() {
        return arrivalNotificationSchedules;
    }
    /**
     * @return the locations
     */
    public List<SingleShipmentLocation> getLocations() {
        return locations;
    }
    /**
     * @return the siblings
     */
    public List<SingleShipmentDto> getSiblings() {
        return siblings;
    }

    /**
     * @return the minTemp
     */
    public double getMinTemp() {
        return minTemp;
    }
    /**
     * @param minTemp the minTemp to set
     */
    public void setMinTemp(final double minTemp) {
        this.minTemp = minTemp;
    }
    /**
     * @return the maxTemp
     */
    public double getMaxTemp() {
        return maxTemp;
    }
    /**
     * @param maxTemp the maxTemp to set
     */
    public void setMaxTemp(final double maxTemp) {
        this.maxTemp = maxTemp;
    }
    /**
     * @return the timeOfFirstReading
     */
    public String getTimeOfFirstReading() {
        return timeOfFirstReading;
    }
    /**
     * @param timeOfFirstReading the timeOfFirstReading to set
     */
    public void setTimeOfFirstReading(final String timeOfFirstReading) {
        this.timeOfFirstReading = timeOfFirstReading;
    }
    /**
     * @return the percentageComplete
     */
    public int getPercentageComplete() {
        return percentageComplete;
    }
    /**
     * @param percentageComplete the percentageComplete to set
     */
    public void setPercentageComplete(final int percentageComplete) {
        this.percentageComplete = percentageComplete;
    }
    public int getTrackerPositionFrontPercent() {
        return getPercentageComplete();
    }
    public int getTrackerPositionLeftPercent() {
        return Math.max(0, 100 - getTrackerPositionFrontPercent());
    }
    /**
     * @return the alertSummary
     */
    public Set<AlertType> getAlertSummary() {
        return alertSummary;
    }
    /**
     * @return
     */
    public String getAlertYetToFire() {
        return alertYetToFire;
    }
    /**
     * @param alertYetToFire the alertYetToFire to set
     */
    public void setAlertYetToFire(final String alertYetToFire) {
        this.alertYetToFire = alertYetToFire;
    }
    /**
     * @return the arrivalNotificationTimeIso
     */
    public String getArrivalNotificationTimeIso() {
        return arrivalNotificationTimeIso;
    }
    /**
     * @param arrivalNotificationTimeIso the arrivalNotificationTimeIso to set
     */
    public void setArrivalNotificationTimeIso(final String arrivalNotificationTimeIso) {
        this.arrivalNotificationTimeIso = arrivalNotificationTimeIso;
    }
    /**
     * @return shutdown time ISO string.
     */
    public String getShutdownTimeIso() {
        return shutdownTimeIso;
    }
    /**
     * @param time shutdown time ISO string.
     */
    public void setShutdownTimeIso(final String time) {
        this.shutdownTimeIso = time;
    }
    /**
     * @return arrival time ISO string.
     */
    public String getArrivalTimeIso() {
        return arrivalTimeIso;
    }
    /**
     * @param time arrival time ISO string.
     */
    public void setArrivalTimeIso(final String time) {
        this.arrivalTimeIso = time;
    }
    /**
     * @return last event time in ISO format.
     */
    public String getLastReadingTimeIso() {
        return lastReadingTimeIso;
    }
    /**
     * @param time last event time in ISO format.
     */
    public void setLastReadingTimeIso(final String time) {
        this.lastReadingTimeIso = time;
    }
    /**
     * @return temperature of last event.
     */
    public double getLastReadingTemperature() {
        return lastReadingTemperature;
    }
    /**
     * @param temperature temperature of last event.
     */
    public void setLastReadingTemperature(final double temperature) {
        this.lastReadingTemperature = temperature;
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
     * @param noAlertsAfterStartMinutes
     */
    public void setNoAlertsAfterStartMinutes(final Integer noAlertsAfterStartMinutes) {
        this.noAlertsAfterStartMinutes = noAlertsAfterStartMinutes;
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
     * @return the startTime
     */
    public String getStartTime() {
        return startTime;
    }

    /**
     * @param startTime the startTime to set
     */
    public void setStartTime(final String startTime) {
        this.startTime = startTime;
    }

    /**
     * @return the etaPretty
     */
    public String getEta() {
        return eta;
    }

    /**
     * @param etaPretty the etaPretty to set
     */
    public void setEta(final String etaPretty) {
        this.eta = etaPretty;
    }

    /**
     * @return the arrivalNotificationTime
     */
    public String getArrivalNotificationTime() {
        return arrivalNotificationTime;
    }

    /**
     * @param arrivalNotificationTime the arrivalNotificationTime to set
     */
    public void setArrivalNotificationTime(final String arrivalNotificationTime) {
        this.arrivalNotificationTime = arrivalNotificationTime;
    }

    /**
     * @return the shutdownTime
     */
    public String getShutdownTime() {
        return shutdownTime;
    }

    /**
     * @param shutdownTime the shutdownTime to set
     */
    public void setShutdownTime(final String shutdownTime) {
        this.shutdownTime = shutdownTime;
    }

    /**
     * @return the arrivalTime
     */
    public String getArrivalTime() {
        return arrivalTime;
    }

    /**
     * @param arrivalTime the arrivalTime to set
     */
    public void setArrivalTime(final String arrivalTime) {
        this.arrivalTime = arrivalTime;
    }

    /**
     * @return the firstReadingTime
     */
    public String getFirstReadingTime() {
        return firstReadingTime;
    }

    /**
     * @param firstReadingTime the firstReadingTime to set
     */
    public void setFirstReadingTime(final String firstReadingTime) {
        this.firstReadingTime = firstReadingTime;
    }

    /**
     * @return the lastReadingTime
     */
    public String getLastReadingTime() {
        return lastReadingTime;
    }

    /**
     * @param lastReadingTime the lastReadingTime to set
     */
    public void setLastReadingTime(final String lastReadingTime) {
        this.lastReadingTime = lastReadingTime;
    }
    /**
     * @return the startLocationAlternatives
     */
    public List<LocationProfile> getStartLocationAlternatives() {
        return startLocationAlternatives;
    }
    /**
     * @return the endLocationAlternatives
     */
    public List<LocationProfile> getEndLocationAlternatives() {
        return endLocationAlternatives;
    }
    /**
     * @return the interimLocationAlternatives
     */
    public List<LocationProfile> getInterimLocationAlternatives() {
        return interimLocationAlternatives;
    }
    /**
     * @return the interimStops
     */
    public List<InterimStop> getInterimStops() {
        return interimStops;
    }
}
