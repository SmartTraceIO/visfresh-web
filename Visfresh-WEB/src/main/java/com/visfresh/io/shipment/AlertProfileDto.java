/**
 *
 */
package com.visfresh.io.shipment;

import com.visfresh.entities.AlertProfile;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class AlertProfileDto {
    private Long id;
    private String name;
    private String description;
    private boolean watchEnterBrightEnvironment;
    private boolean watchEnterDarkEnvironment;
    private boolean watchBatteryLow;
    private boolean watchMovementStart;
    private boolean watchMovementStop;
    private double lowerTemperatureLimit = 0.;
    private double upperTemperatureLimit = 5.;

    public AlertProfileDto(final AlertProfile ap) {
        super();
        setDescription(ap.getDescription());
        setId(ap.getId());
        setLowerTemperatureLimit(ap.getLowerTemperatureLimit());
        setName(ap.getName());
        setUpperTemperatureLimit(ap.getUpperTemperatureLimit());
        setWatchBatteryLow(ap.isWatchBatteryLow());
        setWatchEnterBrightEnvironment(ap.isWatchEnterBrightEnvironment());
        setWatchEnterDarkEnvironment(ap.isWatchEnterDarkEnvironment());
        setWatchMovementStart(ap.isWatchMovementStart());
        setWatchMovementStop(ap.isWatchMovementStop());
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
     * @return the description
     */
    public String getDescription() {
        return description;
    }
    /**
     * @param description the description to set
     */
    public void setDescription(final String description) {
        this.description = description;
    }
    /**
     * @return the watchEnterBrightEnvironment
     */
    public boolean isWatchEnterBrightEnvironment() {
        return watchEnterBrightEnvironment;
    }
    /**
     * @param watchEnterBrightEnvironment the watchEnterBrightEnvironment to set
     */
    public void setWatchEnterBrightEnvironment(final boolean watchEnterBrightEnvironment) {
        this.watchEnterBrightEnvironment = watchEnterBrightEnvironment;
    }
    /**
     * @return the watchEnterDarkEnvironment
     */
    public boolean isWatchEnterDarkEnvironment() {
        return watchEnterDarkEnvironment;
    }
    /**
     * @param watchEnterDarkEnvironment the watchEnterDarkEnvironment to set
     */
    public void setWatchEnterDarkEnvironment(final boolean watchEnterDarkEnvironment) {
        this.watchEnterDarkEnvironment = watchEnterDarkEnvironment;
    }
    /**
     * @return the watchBatteryLow
     */
    public boolean isWatchBatteryLow() {
        return watchBatteryLow;
    }
    /**
     * @param watchBatteryLow the watchBatteryLow to set
     */
    public void setWatchBatteryLow(final boolean watchBatteryLow) {
        this.watchBatteryLow = watchBatteryLow;
    }
    /**
     * @return the watchMovementStart
     */
    public boolean isWatchMovementStart() {
        return watchMovementStart;
    }
    /**
     * @param watchMovementStart the watchMovementStart to set
     */
    public void setWatchMovementStart(final boolean watchMovementStart) {
        this.watchMovementStart = watchMovementStart;
    }
    /**
     * @return the watchMovementStop
     */
    public boolean isWatchMovementStop() {
        return watchMovementStop;
    }
    /**
     * @param watchMovementStop the watchMovementStop to set
     */
    public void setWatchMovementStop(final boolean watchMovementStop) {
        this.watchMovementStop = watchMovementStop;
    }
    /**
     * @return the lowerTemperatureLimit
     */
    public double getLowerTemperatureLimit() {
        return lowerTemperatureLimit;
    }
    /**
     * @param lowerTemperatureLimit the lowerTemperatureLimit to set
     */
    public void setLowerTemperatureLimit(final double lowerTemperatureLimit) {
        this.lowerTemperatureLimit = lowerTemperatureLimit;
    }
    /**
     * @return the upperTemperatureLimit
     */
    public double getUpperTemperatureLimit() {
        return upperTemperatureLimit;
    }
    /**
     * @param upperTemperatureLimit the upperTemperatureLimit to set
     */
    public void setUpperTemperatureLimit(final double upperTemperatureLimit) {
        this.upperTemperatureLimit = upperTemperatureLimit;
    }
}
