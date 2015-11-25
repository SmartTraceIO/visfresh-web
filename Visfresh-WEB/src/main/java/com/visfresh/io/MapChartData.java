/**
 *
 */
package com.visfresh.io;

import java.util.LinkedList;
import java.util.List;

import com.visfresh.entities.Location;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class MapChartData {
    /**
     * Name of start location.
     */
    private String startLocation;
    /**
     * Start time as string "19:00 12 AUG 14"
     */
    private String startTimeStr;
    /**
     * Start time in ISO format "2014-08-12 12:10"
     */
    private String startTimeISO;
    /**
     * Name of end location;
     */
    private String endLocation;
    /**
     * Estimation time for arrival in ISO format "2014-08-12 21:40"
     */
    private String eta;
    /**
     * Estimation time for arrival string "21:40 12 AUG 14"
     */
    private String etaStr;
    /**
     * Current location description.
     */
    private String currentLocation;
    /**
     * Start location coordinates.
     * lat: 13.929930,
     * long: 75.568100,
     */
    private Location startLocationForMap;
    /**
     * End location coordinates.
     * lat: 26.536938,
     * long: 80.489960,
     */
    private Location endLocationForMap;
    /**
     * Current location coordinates.
     * lat: 26.536938,
     * long: 80.489960,
     */
    private Location currentLocationForMap;

    private final List<MapChartItem> locations = new LinkedList<MapChartItem>();
    /**
     * Default constructor.
     */
    public MapChartData() {
        super();
    }

    /**
     * @return the items
     */
    public List<MapChartItem> getLocations() {
        return locations;
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
}
