/**
 *
 */
package com.visfresh.io.shipment;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import com.visfresh.entities.Location;
import com.visfresh.entities.ShipmentStatus;
import com.visfresh.lists.ListNotificationScheduleItem;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class SingleShipmentBean {
    private long shipmentId;
    private Long companyId;
    private String device;
    private String deviceName;
    private int tripCount;
    private String shipmentDescription;
    private String palletId;
    private String assetNum;
    private String assetType;
    private ShipmentStatus status;
    private Integer alertSuppressionMinutes;
    private final List<ListNotificationScheduleItem> alertsNotificationSchedules = new LinkedList<>();
    private String commentsForReceiver;
    private Integer arrivalNotificationWithinKm;
    boolean excludeNotificationsIfNoAlerts;
    private final List<ListNotificationScheduleItem> arrivalNotificationSchedules = new LinkedList<>();
    private boolean sendArrivalReport = true;
    private boolean sendArrivalReportOnlyIfAlerts = false;
    private Integer shutdownDeviceAfterMinutes;
    private Integer noAlertsAfterArrivalMinutes;
    private Integer shutDownAfterStartMinutes;
    private LocationProfileBean startLocation;
    private Date startTime;
    private LocationProfileBean endLocation;
    private Date eta;
    private Location currentLocation;
    private String currentLocationDescription = "Not determined";
    private int percentageComplete;

    private double minTemp;
    private double maxTemp;

    private final List<Long> siblings = new LinkedList<>();
    private final List<AlertRuleBean> alertYetToFire = new LinkedList<>();
    private final List<AlertRuleBean> alertFired = new LinkedList<>();
    private ArrivalBean arrival;
    private Date shutdownTime;
    private Date arrivalTime;
    private boolean alertsSuppressed;
    private Date alertsSuppressionTime;
    private Date firstReadingTime;
    private Date lastReadingTime;
    private double lastReadingTemperature;
    private Integer batteryLevel;
    private Integer noAlertsAfterStartMinutes;
    private String shipmentType;

    private final List<LocationProfileBean> startLocationAlternatives = new LinkedList<>();
    private final List<LocationProfileBean> endLocationAlternatives = new LinkedList<>();
    private final List<LocationProfileBean> interimLocationAlternatives = new LinkedList<>();
    private final List<InterimStopBean> interimStops = new LinkedList<>();
    private final List<NoteBean> notes = new LinkedList<>();
    private final List<DeviceGroupDto> deviceGroups = new LinkedList<>();
    private String deviceColor;
    private boolean isLatestShipment;
    private boolean arrivalReportSent;
    private final List<ShipmentUserDto> userAccess = new LinkedList<>();
    private final List<ShipmentCompanyDto> companyAccess = new LinkedList<>();
    private final List<AlertBean> sentAlerts = new LinkedList<>();
    private AlertProfileBean alertProfile;

    /**
     * Default constructor.
     */
    public SingleShipmentBean() {
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
    public String getDevice() {
        return device;
    }
    /**
     * @param device the deviceSN to set
     */
    public void setDevice(final String device) {
        this.device = device;
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
     * @param c device color.
     */
    public void setDeviceColor(final String c) {
        this.deviceColor = c;
    }
    /**
     * @return the deviceColor
     */
    public String getDeviceColor() {
        return deviceColor;
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
    public LocationProfileBean getStartLocation() {
        return startLocation;
    }
    /**
     * @param startLocation the startLocation to set
     */
    public void setStartLocation(final LocationProfileBean startLocation) {
        this.startLocation = startLocation;
    }
    /**
     * @return the endLocation
     */
    public LocationProfileBean getEndLocation() {
        return endLocation;
    }
    /**
     * @param endLocation the endLocation to set
     */
    public void setEndLocation(final LocationProfileBean endLocation) {
        this.endLocation = endLocation;
    }
    /**
     * @return the currentLocation
     */
    public Location getCurrentLocation() {
        return currentLocation;
    }
    /**
     * @param currentLocation the currentLocation to set
     */
    public void setCurrentLocation(final Location currentLocation) {
        this.currentLocation = currentLocation;
    }
    /**
     * @return the currentLocationDescription
     */
    public String getCurrentLocationDescription() {
        return currentLocationDescription;
    }
    /**
     * @param currentLocationDescription the currentLocationDescription to set
     */
    public void setCurrentLocationDescription(final String currentLocationDescription) {
        this.currentLocationDescription = currentLocationDescription;
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
     * @return the siblings
     */
    public List<Long> getSiblings() {
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
     * @return
     */
    public List<AlertRuleBean> getAlertYetToFire() {
        return alertYetToFire;
    }
    /**
     * @return the alertFired
     */
    public List<AlertRuleBean> getAlertFired() {
        return alertFired;
    }
    /**
     * @return the batteryLevel
     */
    public Integer getBatteryLevel() {
        return batteryLevel;
    }
    /**
     * @param batteryLevel the batteryLevel to set
     */
    public void setBatteryLevel(final Integer batteryLevel) {
        this.batteryLevel = batteryLevel;
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
    public Date getStartTime() {
        return startTime;
    }

    /**
     * @param startTime the startTime to set
     */
    public void setStartTime(final Date startTime) {
        this.startTime = startTime;
    }

    /**
     * @return the etaPretty
     */
    public Date getEta() {
        return eta;
    }

    /**
     * @param etaPretty the etaPretty to set
     */
    public void setEta(final Date etaPretty) {
        this.eta = etaPretty;
    }

    /**
     * @return the arrival
     */
    public ArrivalBean getArrival() {
        return arrival;
    }
    /**
     * @param arrival the arrival to set
     */
    public void setArrival(final ArrivalBean arrival) {
        this.arrival = arrival;
    }

    /**
     * @return the shutdownTime
     */
    public Date getShutdownTime() {
        return shutdownTime;
    }

    /**
     * @param shutdownTime the shutdownTime to set
     */
    public void setShutdownTime(final Date shutdownTime) {
        this.shutdownTime = shutdownTime;
    }

    /**
     * @return the arrivalTime
     */
    public Date getArrivalTime() {
        return arrivalTime;
    }

    /**
     * @param arrivalTime the arrivalTime to set
     */
    public void setArrivalTime(final Date arrivalTime) {
        this.arrivalTime = arrivalTime;
    }

    /**
     * @return the firstReadingTime
     */
    public Date getFirstReadingTime() {
        return firstReadingTime;
    }

    /**
     * @param firstReadingTime the firstReadingTime to set
     */
    public void setFirstReadingTime(final Date firstReadingTime) {
        this.firstReadingTime = firstReadingTime;
    }

    /**
     * @return the lastReadingTime
     */
    public Date getLastReadingTime() {
        return lastReadingTime;
    }

    /**
     * @param lastReadingTime the lastReadingTime to set
     */
    public void setLastReadingTime(final Date lastReadingTime) {
        this.lastReadingTime = lastReadingTime;
    }
    /**
     * @return the startLocationAlternatives
     */
    public List<LocationProfileBean> getStartLocationAlternatives() {
        return startLocationAlternatives;
    }
    /**
     * @return the endLocationAlternatives
     */
    public List<LocationProfileBean> getEndLocationAlternatives() {
        return endLocationAlternatives;
    }
    /**
     * @return the interimLocationAlternatives
     */
    public List<LocationProfileBean> getInterimLocationAlternatives() {
        return interimLocationAlternatives;
    }
    /**
     * @return the interimStops
     */
    public List<InterimStopBean> getInterimStops() {
        return interimStops;
    }
    /**
     * @return the sthipmentType
     */
    public String getShipmentType() {
        return shipmentType;
    }
    /**
     * @param shipmentType the shipmentType to set
     */
    public void setShipmentType(final String shipmentType) {
        this.shipmentType = shipmentType;
    }
    /**
     * @return
     */
    public List<NoteBean> getNotes() {
        return notes;
    }

    /**
     * @return the alertsSuppressed
     */
    public boolean isAlertsSuppressed() {
        return alertsSuppressed;
    }

    /**
     * @param alertsSuppressed the alertsSuppressed to set
     */
    public void setAlertsSuppressed(final boolean alertsSuppressed) {
        this.alertsSuppressed = alertsSuppressed;
    }

    /**
     * @return the alertsSuppressionTime
     */
    public Date getAlertsSuppressionTime() {
        return alertsSuppressionTime;
    }

    /**
     * @param alertsSuppressionTime the alertsSuppressionTime to set
     */
    public void setAlertsSuppressionTime(final Date alertsSuppressionTime) {
        this.alertsSuppressionTime = alertsSuppressionTime;
    }
    /**
     * @return the deviceGroups
     */
    public List<DeviceGroupDto> getDeviceGroups() {
        return deviceGroups;
    }
    /**
     * @param latest
     */
    public void setLatestShipment(final boolean latest) {
        isLatestShipment = latest;
    }
    /**
     * @return the isLatestShipment
     */
    public boolean isLatestShipment() {
        return isLatestShipment;
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
    /**
     * @return the arrivalReportSent
     */
    public boolean isArrivalReportSent() {
        return arrivalReportSent;
    }
    /**
     * @param arrivalReportSent the arrivalReportSent to set
     */
    public void setArrivalReportSent(final boolean arrivalReportSent) {
        this.arrivalReportSent = arrivalReportSent;
    }
    /**
     * @return the userAccess
     */
    public List<ShipmentUserDto> getUserAccess() {
        return userAccess;
    }
    /**
     * @return the companyAccess
     */
    public List<ShipmentCompanyDto> getCompanyAccess() {
        return companyAccess;
    }
    /**
     * @return the sentAlerts
     */
    public List<AlertBean> getSentAlerts() {
        return sentAlerts;
    }

    /**
     * @param alertProfileDto
     */
    public void setAlertProfile(final AlertProfileBean alertProfileDto) {
        this.alertProfile = alertProfileDto;
    }
    /**
     * @return the alertProfile
     */
    public AlertProfileBean getAlertProfile() {
        return alertProfile;
    }
    /**
     * @return
     */
    public Long getCompanyId() {
        return companyId;
    }
    /**
     * @param companyId the companyId to set
     */
    public void setCompanyId(final Long companyId) {
        this.companyId = companyId;
    }
}
