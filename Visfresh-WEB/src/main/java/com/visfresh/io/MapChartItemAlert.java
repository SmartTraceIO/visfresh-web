/**
 *
 */
package com.visfresh.io;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class MapChartItemAlert {
    /**
     * Alert title "Too cold alert: #654321 went below 1c"
     */
    private String title;
    /**
     * Temperature
     */
    private double temperature;
    /**
     * Time in short format "6:47pm".
     */
    private String time;
    /**
     * Date in ISO format "2014-10-11T12:30"
     */
    private String date;
    /**
     * "Close to Orbost, Vic(+/-4000m)"
     */
    private String location;
    /**
     * Shipped to address "GHJ Store Petersham"
     */
    private String shippedTo;
    /**
     * Estimated time arrival "2014-10-11T13:30"
     */
    private String eta;
    /**
     * Alert type.
     */
    private String type;

    /**
     * Default constructor.
     */
    public MapChartItemAlert() {
        super();
    }

    /**
     * @return the title
     */
    public String getTitle() {
        return title;
    }
    /**
     * @param title the title to set
     */
    public void setTitle(final String title) {
        this.title = title;
    }
    /**
     * @return the temprature
     */
    public double getTemperature() {
        return temperature;
    }
    /**
     * @param temprature the temprature to set
     */
    public void setTemeprature(final double temprature) {
        this.temperature = temprature;
    }
    /**
     * @return the time
     */
    public String getTime() {
        return time;
    }
    /**
     * @param time the time to set
     */
    public void setTime(final String time) {
        this.time = time;
    }
    /**
     * @return the date
     */
    public String getDate() {
        return date;
    }
    /**
     * @param date the date to set
     */
    public void setDate(final String date) {
        this.date = date;
    }
    /**
     * @return the location
     */
    public String getLocation() {
        return location;
    }
    /**
     * @param location the location to set
     */
    public void setLocation(final String location) {
        this.location = location;
    }
    /**
     * @return the shippedTo
     */
    public String getShippedTo() {
        return shippedTo;
    }
    /**
     * @param shippedTo the shippedTo to set
     */
    public void setShippedTo(final String shippedTo) {
        this.shippedTo = shippedTo;
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
     * @return the type
     */
    public String getType() {
        return type;
    }
    /**
     * @param type the type to set
     */
    public void setType(final String type) {
        this.type = type;
    }
}
