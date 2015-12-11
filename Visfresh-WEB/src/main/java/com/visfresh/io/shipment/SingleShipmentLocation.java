/**
 *
 */
package com.visfresh.io.shipment;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class SingleShipmentLocation {
    private double latitude;
    private double longitude;
    private double temperature;
    private String timeIso;
    private SingleShipmentAlert alert;

    /**
     * Default constructor.
     */
    public SingleShipmentLocation() {
        super();
    }

    /**
     * @return the latitude
     */
    public double getLatitude() {
        return latitude;
    }
    /**
     * @param latitude the latitude to set
     */
    public void setLatitude(final double latitude) {
        this.latitude = latitude;
    }
    /**
     * @return the longitude
     */
    public double getLongitude() {
        return longitude;
    }
    /**
     * @param longitude the longitude to set
     */
    public void setLongitude(final double longitude) {
        this.longitude = longitude;
    }
    /**
     * @return the temperature
     */
    public double getTemperature() {
        return temperature;
    }
    /**
     * @param temperature the temperature to set
     */
    public void setTemperature(final double temperature) {
        this.temperature = temperature;
    }
    /**
     * @return the timeIso
     */
    public String getTimeIso() {
        return timeIso;
    }
    /**
     * @param timeIso the timeIso to set
     */
    public void setTimeIso(final String timeIso) {
        this.timeIso = timeIso;
    }
    /**
     * @return the alert
     */
    public SingleShipmentAlert getAlert() {
        return alert;
    }
    /**
     * @param alert the alert to set
     */
    public void setAlert(final SingleShipmentAlert alert) {
        this.alert = alert;
    }
}
