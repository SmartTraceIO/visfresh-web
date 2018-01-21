/**
 *
 */
package com.visfresh.entities;

import java.util.LinkedList;
import java.util.List;


/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class AlertProfile implements EntityWithId<Long>, EntityWithCompany {
    public static final double DEFAULT_LOWER_TEMPERATURE_LIMIT = 0.0;
    public static final double DEFAULT_UPPER_TEMPERATURE_LIMIT = 5.;
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
     * List of temperature issues.
     */
    private List<TemperatureRule> alertRules = new LinkedList<TemperatureRule>();
    /**
     * Alert for enter bright environment.
     */
    private boolean watchEnterBrightEnvironment;
    private CorrectiveActionList lightOnCorrectiveActions;
    /**
     * Alert for enter dark environment.
     */
    private boolean watchEnterDarkEnvironment;
    /**
     * Alert for batterry low.
     */
    private boolean watchBatteryLow;
    private CorrectiveActionList batteryLowCorrectiveActions;
    private boolean watchMovementStart;
    private boolean watchMovementStop;

    private double lowerTemperatureLimit = DEFAULT_LOWER_TEMPERATURE_LIMIT;
    private double upperTemperatureLimit = DEFAULT_UPPER_TEMPERATURE_LIMIT;

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
     * @return the company
     */
    @Override
    public Company getCompany() {
        return company;
    }
    /**
     * @param company the company to set
     */
    @Override
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
     * @return the temperatureIssues
     */
    public List<TemperatureRule> getAlertRules() {
        return alertRules;
    }
    /**
     * @return the loverTemperatureLimit
     */
    public double getLowerTemperatureLimit() {
        return lowerTemperatureLimit;
    }
    /**
     * @param lowerTemperatureLimit the loverTemperatureLimit to set
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
    /**
     * @return the batteryLowCorrectiveActions
     */
    public CorrectiveActionList getBatteryLowCorrectiveActions() {
        return batteryLowCorrectiveActions;
    }
    /**
     * @param batteryLowCorrectiveActions the batteryLowCorrectiveActions to set
     */
    public void setBatteryLowCorrectiveActions(final CorrectiveActionList batteryLowCorrectiveActions) {
        this.batteryLowCorrectiveActions = batteryLowCorrectiveActions;
    }
    /**
     * @return the lightOnCorrectiveActions
     */
    public CorrectiveActionList getLightOnCorrectiveActions() {
        return lightOnCorrectiveActions;
    }
    /**
     * @param lightOnCorrectiveActions the lightOnCorrectiveActions to set
     */
    public void setLightOnCorrectiveActions(final CorrectiveActionList lightOnCorrectiveActions) {
        this.lightOnCorrectiveActions = lightOnCorrectiveActions;
    }
}
