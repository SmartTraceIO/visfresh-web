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
    Battery;

    /**
     * @return true if the alert type is temperature alert.
     */
    public boolean isTemperatureAlert() {
        switch (this) {
            case Hot:
            case CriticalHot:
            case Cold:
            case CriticalCold:
                return true;
            default:
                return false;
        }
    }
}
