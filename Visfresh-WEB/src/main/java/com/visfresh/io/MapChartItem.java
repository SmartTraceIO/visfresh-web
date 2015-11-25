/**
 *
 */
package com.visfresh.io;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class MapChartItem {
    /**
     * Latitude.
     */
    private double lat;
    /**
     * Longitude.
     */
    private double lon;
    /**
     * Temperature.
     */
    private double temperature;
    /**
     * Time in ISO format.
     */
    private String timeISO;
    /**
     * Alert
     */
    private MapChartItemAlert alert;

    /**
     * Default constructor.
     */
    public MapChartItem() {
        super();
    }

    /**
     * @return the lat
     */
    public double getLat() {
        return lat;
    }
    /**
     * @param lat the lat to set
     */
    public void setLat(final double lat) {
        this.lat = lat;
    }
    /**
     * @return the lon
     */
    public double getLon() {
        return lon;
    }
    /**
     * @param lon the lon to set
     */
    public void setLon(final double lon) {
        this.lon = lon;
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
     * @return the timeISO
     */
    public String getTimeISO() {
        return timeISO;
    }
    /**
     * @param timeISO the timeISO to set
     */
    public void setTimeISO(final String timeISO) {
        this.timeISO = timeISO;
    }
    /**
     * @return the alert
     */
    public MapChartItemAlert getAlert() {
        return alert;
    }
    /**
     * @param alert the alert to set
     */
    public void setAlert(final MapChartItemAlert alert) {
        this.alert = alert;
    }
}
