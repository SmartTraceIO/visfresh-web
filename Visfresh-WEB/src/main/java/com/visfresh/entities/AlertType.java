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
    Cold,
    Hot,
    CriticalCold,
    CriticalHot,
    //other alerts
    LightOn,
    LightOff,
    MovementStart,
    MovementStop,
    Battery,
}
