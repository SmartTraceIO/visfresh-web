/**
 *
 */
package com.visfresh.entities;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public enum TemperatureUnits {
    Celsius("C"),
    Fahrenheit("F");

    private String unit;
    TemperatureUnits(final String unit) {
        this.unit = unit;
    }
    /**
     * @return the unit
     */
    public String getUnit() {
        return unit;
    }
}
