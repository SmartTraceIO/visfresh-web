/**
 *
 */
package com.visfresh.entities;

import javax.persistence.Column;
import javax.persistence.ConstraintMode;
import javax.persistence.Entity;
import javax.persistence.ForeignKey;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Entity
@Table(name="alertprofiles")
public class AlertProfile implements EntityWithId {
    /**
     * ID in data base.
     */
    @Id
    @Column(name = "id", columnDefinition="BIGINT AUTO_INCREMENT")
    @GeneratedValue(strategy = GenerationType.TABLE)
    private Long id;
    @ManyToOne(optional = false)
    @JoinColumn(name = "company",
        foreignKey = @ForeignKey(value = ConstraintMode.CONSTRAINT),
        columnDefinition = "bigint",
        referencedColumnName = "id")
    private Company company;
    /**
     * Alert profile name.
     */
    @Column(nullable = false)
    private String name;
    /**
     * Alert profile description.
     */
    @Column
    private String description;
    /**
     * The low temperature.
     */
    @Column(name = "lowtemp")
    private double lowTemperature;
    /**
     * Critical low temperature
     */
    @Column(name = "lowtempcritical")
    private double criticalLowTemperature;
    /**
     * Alert for low temperature more then given value in minutes.
     */
    @Column(name = "lowtempmorethen")
    private int lowTemperatureForMoreThen;
    /**
     * Alert for critical low temperature more then given value in minutes.
     */
    @Column(name = "criticallowtempmorethen")
    private int criticalLowTemperatureForMoreThen;
    /**
     * High temperature
     */
    @Column(name = "hightemp")
    private double highTemperature;
    /**
     * Critical high temperature
     */
    @Column(name = "hightempcritical")
    private double criticalHighTemperature;
    /**
     * Alert for high temperature more then given value in minutes.
     */
    @Column(name = "hightempmorethen")
    private int highTemperatureForMoreThen;
    /**
     * Alert for critical high temperature more then given value in minutes.
     */
    @Column(name = "criticalhightempmorethen")
    private int criticalHighTemperatureForMoreThen;
    /**
     * Alert for enter bright environment.
     */
    @Column(name = "enterbrightenv")
    private boolean watchEnterBrightEnvironment;
    /**
     * Alert for enter dark environment.
     */
    @Column(name = "enterdarktenv")
    private boolean watchEnterDarkEnvironment;
    /**
     * Alert for shock
     */
    @Column(name = "onshock")
    private boolean watchShock;
    /**
     * Alert for baterry low.
     */
    @Column(name = "onsbatterylow")
    private boolean watchBatteryLow;

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
     * @return the watchShock
     */
    public boolean isWatchShock() {
        return watchShock;
    }
    /**
     * @param watchShock the watchShock to set
     */
    public void setWatchShock(final boolean watchShock) {
        this.watchShock = watchShock;
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
    public Company getCompany() {
        return company;
    }
    /**
     * @param company the company to set
     */
    public void setCompany(final Company company) {
        this.company = company;
    }
}
