/**
 *
 */
package com.visfresh.io.shipment;

import java.util.LinkedList;
import java.util.List;

import com.visfresh.entities.Location;
import com.visfresh.entities.ShipmentStatus;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class SingleShipmentDtoNew {
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
    private final List<Long> alertsNotificationSchedules = new LinkedList<Long>();
    private String commentsForReceiver;
    private Integer arrivalNotificationWithinKm;
    boolean excludeNotificationsIfNoAlerts;
    private final List<Long> arrivalNotificationSchedules = new LinkedList<Long>();
    private Integer shutdownDeviceAfterMinutes;
    private String startLocation;
    private String startTimeStr;
    private String startTimeISO;
    private String endLocation;
    private String eta;
    private String etaStr;
    private String currentLocation;
    private Location startLocationForMap;
    private Location endLocationForMap;
    private Location currentLocationForMap;

    private double minTemp;
    private double maxTemp;
    private String timeOfFirstReading;
    private String timeOfLastReading;

    private final List<SingleShipmentLocation> locations = new LinkedList<>();
    private final List<SingleShipmentDtoNew> siblings = new LinkedList<>();

    /**
     * Default constructor.
     */
    public SingleShipmentDtoNew() {
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
     * @return the startTimeStr
     */
    public String getStartTimeStr() {
        return startTimeStr;
    }
    /**
     * @param startTimeStr the startTimeStr to set
     */
    public void setStartTimeStr(final String startTimeStr) {
        this.startTimeStr = startTimeStr;
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
    public String getEta() {
        return eta;
    }
    /**
     * @param eta the eta to set
     */
    public void setEta(final String eta) {
        this.eta = eta;
    }
    /**
     * @return the etaStr
     */
    public String getEtaStr() {
        return etaStr;
    }
    /**
     * @param etaStr the etaStr to set
     */
    public void setEtaStr(final String etaStr) {
        this.etaStr = etaStr;
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
     * @return the locations
     */
    public List<SingleShipmentLocation> getLocations() {
        return locations;
    }
    /**
     * @return the siblings
     */
    public List<SingleShipmentDtoNew> getSiblings() {
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
     * @return the timeOfLastReading
     */
    public String getTimeOfLastReading() {
        return timeOfLastReading;
    }
    /**
     * @param timeOfLastReading the timeOfLastReading to set
     */
    public void setTimeOfLastReading(final String timeOfLastReading) {
        this.timeOfLastReading = timeOfLastReading;
    }
}
