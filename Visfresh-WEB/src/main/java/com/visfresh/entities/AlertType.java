/**
 *
 */
package com.visfresh.entities;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public enum AlertType {
    //temperature alerts
    LowTemperature,
    HighTemperature,
    CriticalLowTemperature,
    CriticalHighTemperature,
    //other alerts
    EnterBrightEnvironment,
    EnterDarkEnvironment,
    Shock,
    BatteryLow
}
