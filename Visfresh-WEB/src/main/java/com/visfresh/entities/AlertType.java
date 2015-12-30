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
    Hot,
    CriticalHot,
    Cold,
    CriticalCold,
    //other alerts
    MovementStart,
    LightOn,
    LightOff,
//    MovementStop,
    Battery
}
