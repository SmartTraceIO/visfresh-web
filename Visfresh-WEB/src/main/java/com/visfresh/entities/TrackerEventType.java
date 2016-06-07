/**
 *
 */
package com.visfresh.entities;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public enum TrackerEventType {
    /**
     * means auto-collected-data at device starting,
     */
    INIT,
    /**
     * means auto-collected-data by timer
     */
    AUT,
    /**
     * means the RESPONSE that is replied to server
     */
    RSP,
    /**
     * means the device start to vibrating
     */
    VIB,
    /**
     * means the device is stable
     */
    STP,
    /**
     * means the device enters bright environment
     */
    BRT,
    /**
     * means the device enters dark environment
     */
    DRK,
    /**
     * the charger is not inserted and average battery voltage is less than 3400mV
     */
    BAT0,
    /**
     * the charger is not inserted and average battery voltage is less than 3500mV
     */
    BAT1,
    /**
     * the charger is not inserted and average battery voltage is more than 3500mV,
     */
    BAT2,
    /**
     * the charger is inserted and battery is charging
     */
    CRG0,
    /**
     * the charger is inserted and battery is full
     */
    CRG1,
    /**
     * Undefined message type.
     */
    UNDEF
}
