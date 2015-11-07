/**
 *
 */
package com.visfresh.entities;


/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class AlertProfile implements EntityWithId<Long>, EntityWithCompany {
    /**
     * ID.
     */
    private Long id;
    /**
     * ID in data base.
     */
    private Company company;
    /**
     * Alert profile name.
     */
    private String name;
    /**
     * Alert profile description.
     */
    private String description;
    /**
     * The low temperature.
     */
    private double lowTemperature;
    /**
     * Alert for low temperature more then given value in minutes.
     */
    private int lowTemperatureForMoreThen;
    /**
     * The low temperature.
     */
    private Double lowTemperature2;
    /**
     * Alert for low temperature more then given value in minutes.
     */
    private Integer lowTemperatureForMoreThen2;
    /**
     * Critical low temperature
     */
    private double criticalLowTemperature;
    /**
     * Alert for critical low temperature more then given value in minutes.
     */
    private int criticalLowTemperatureForMoreThen;
    /**
     * Critical low temperature
     */
    private Double criticalLowTemperature2;
    /**
     * Alert for critical low temperature more then given value in minutes.
     */
    private Integer criticalLowTemperatureForMoreThen2;
    /**
     * High temperature
     */
    private double highTemperature;
    /**
     * Alert for high temperature more then given value in minutes.
     */
    private int highTemperatureForMoreThen;
    /**
     * High temperature
     */
    private Double highTemperature2;
    /**
     * Alert for high temperature more then given value in minutes.
     */
    private Integer highTemperatureForMoreThen2;
    /**
     * Critical high temperature
     */
    private double criticalHighTemperature;
    /**
     * Alert for critical high temperature more then given value in minutes.
     */
    private int criticalHighTemperatureForMoreThen;
    /**
     * Critical high temperature
     */
    private Double criticalHighTemperature2;
    /**
     * Alert for critical high temperature more then given value in minutes.
     */
    private Integer criticalHighTemperatureForMoreThen2;
    /**
     * Alert for enter bright environment.
     */
    private boolean watchEnterBrightEnvironment;
    /**
     * Alert for enter dark environment.
     */
    private boolean watchEnterDarkEnvironment;
    /**
     * Alert for batterry low.
     */
    private boolean watchBatteryLow;
    private boolean watchMovementStart;
    private boolean watchMovementStop;

    /**
     * Default constructor.
     */
    public AlertProfile() {
        super();
    }

    /**
     * @return the id
     */
    @Override
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
     * @return the lowTemperature
     */
    public double getLowTemperature() {
        return lowTemperature;
    }
    /**
     * @param lowTemperature the lowTemperature to set
     */
    public void setLowTemperature(final double lowTemperature) {
        this.lowTemperature = lowTemperature;
    }
    /**
     * @return the criticalLowTemperature
     */
    public double getCriticalLowTemperature() {
        return criticalLowTemperature;
    }
    /**
     * @param criticalLowTemperature the criticalLowTemperature to set
     */
    public void setCriticalLowTemperature(final double criticalLowTemperature) {
        this.criticalLowTemperature = criticalLowTemperature;
    }
    /**
     * @return the lowTemperatureForMoreThen
     */
    public int getLowTemperatureForMoreThen() {
        return lowTemperatureForMoreThen;
    }
    /**
     * @param lowTemperatureForMoreThen the lowTemperatureForMoreThen to set
     */
    public void setLowTemperatureForMoreThen(final int lowTemperatureForMoreThen) {
        this.lowTemperatureForMoreThen = lowTemperatureForMoreThen;
    }
    /**
     * @return the highTemperature
     */
    public double getHighTemperature() {
        return highTemperature;
    }
    /**
     * @param highTemperature the highTemperature to set
     */
    public void setHighTemperature(final double highTemperature) {
        this.highTemperature = highTemperature;
    }
    /**
     * @return the criticalHighTemperature
     */
    public double getCriticalHighTemperature() {
        return criticalHighTemperature;
    }
    /**
     * @param criticalHighTemperature the criticalHighTemperature to set
     */
    public void setCriticalHighTemperature(final double criticalHighTemperature) {
        this.criticalHighTemperature = criticalHighTemperature;
    }
    /**
     * @return the highTemperatureForMoreThen
     */
    public int getHighTemperatureForMoreThen() {
        return highTemperatureForMoreThen;
    }
    /**
     * @param highTemperatureForMoreThen the highTemperatureForMoreThen to set
     */
    public void setHighTemperatureForMoreThen(final int highTemperatureForMoreThen) {
        this.highTemperatureForMoreThen = highTemperatureForMoreThen;
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
     * @return the criticalHighTemperatureForMoreThen
     */
    public int getCriticalHighTemperatureForMoreThen() {
        return criticalHighTemperatureForMoreThen;
    }
    /**
     * @return the criticalLowTemperatureForMoreThen
     */
    public int getCriticalLowTemperatureForMoreThen() {
        return criticalLowTemperatureForMoreThen;
    }
    /**
     * @param criticalHighTemperatureForMoreThen the criticalHighTemperatureForMoreThen to set
     */
    public void setCriticalHighTemperatureForMoreThen(
            final int criticalHighTemperatureForMoreThen) {
        this.criticalHighTemperatureForMoreThen = criticalHighTemperatureForMoreThen;
    }
    /**
     * @param criticalLowTemperatureForMoreThen the criticalLowTemperatureForMoreThen to set
     */
    public void setCriticalLowTemperatureForMoreThen(
            final int criticalLowTemperatureForMoreThen) {
        this.criticalLowTemperatureForMoreThen = criticalLowTemperatureForMoreThen;
    }
    /**
     * @return the company
     */
    @Override
    public Company getCompany() {
        return company;
    }
    /**
     * @param company the company to set
     */
    public void setCompany(final Company company) {
        this.company = company;
    }
    /**
     * @return
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
     * @return
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
     * @return the lowTemperature2
     */
    public Double getLowTemperature2() {
        return lowTemperature2;
    }
    /**
     * @param lowTemperature2 the lowTemperature2 to set
     */
    public void setLowTemperature2(final Double lowTemperature2) {
        this.lowTemperature2 = lowTemperature2;
    }
    /**
     * @return the lowTemperatureForMoreThen2
     */
    public Integer getLowTemperatureForMoreThen2() {
        return lowTemperatureForMoreThen2;
    }
    /**
     * @param lowTemperatureForMoreThen2 the lowTemperatureForMoreThen2 to set
     */
    public void setLowTemperatureForMoreThen2(final Integer lowTemperatureForMoreThen2) {
        this.lowTemperatureForMoreThen2 = lowTemperatureForMoreThen2;
    }
    /**
     * @return the criticalLowTemperature2
     */
    public Double getCriticalLowTemperature2() {
        return criticalLowTemperature2;
    }
    /**
     * @param criticalLowTemperature2 the criticalLowTemperature2 to set
     */
    public void setCriticalLowTemperature2(final Double criticalLowTemperature2) {
        this.criticalLowTemperature2 = criticalLowTemperature2;
    }
    /**
     * @return the criticalLowTemperatureForMoreThen2
     */
    public Integer getCriticalLowTemperatureForMoreThen2() {
        return criticalLowTemperatureForMoreThen2;
    }
    /**
     * @param criticalLowTemperatureForMoreThen2 the criticalLowTemperatureForMoreThen2 to set
     */
    public void setCriticalLowTemperatureForMoreThen2(
            final Integer criticalLowTemperatureForMoreThen2) {
        this.criticalLowTemperatureForMoreThen2 = criticalLowTemperatureForMoreThen2;
    }
    /**
     * @return the highTemperature2
     */
    public Double getHighTemperature2() {
        return highTemperature2;
    }
    /**
     * @param highTemperature2 the highTemperature2 to set
     */
    public void setHighTemperature2(final Double highTemperature2) {
        this.highTemperature2 = highTemperature2;
    }
    /**
     * @return the highTemperatureForMoreThen2
     */
    public Integer getHighTemperatureForMoreThen2() {
        return highTemperatureForMoreThen2;
    }
    /**
     * @param highTemperatureForMoreThen2 the highTemperatureForMoreThen2 to set
     */
    public void setHighTemperatureForMoreThen2(final Integer highTemperatureForMoreThen2) {
        this.highTemperatureForMoreThen2 = highTemperatureForMoreThen2;
    }
    /**
     * @return the criticalHighTemperature2
     */
    public Double getCriticalHighTemperature2() {
        return criticalHighTemperature2;
    }
    /**
     * @param criticalHighTemperature2 the criticalHighTemperature2 to set
     */
    public void setCriticalHighTemperature2(final Double criticalHighTemperature2) {
        this.criticalHighTemperature2 = criticalHighTemperature2;
    }
    /**
     * @return the criticalHighTemperatureForMoreThen2
     */
    public Integer getCriticalHighTemperatureForMoreThen2() {
        return criticalHighTemperatureForMoreThen2;
    }
    /**
     * @param criticalHighTemperatureForMoreThen2 the criticalHighTemperatureForMoreThen2 to set
     */
    public void setCriticalHighTemperatureForMoreThen2(
            final Integer criticalHighTemperatureForMoreThen2) {
        this.criticalHighTemperatureForMoreThen2 = criticalHighTemperatureForMoreThen2;
    }
}
