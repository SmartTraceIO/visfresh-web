/**
 *
 */
package com.visfresh.entities;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public enum DeviceModel {
    SmartTrace,
    TT18,
    BT04(true),
    TEEL, //TODO remove
    BEEL(true), // TODO remove
    STF2, // is EEL tracker
    STB1(true); //is EEL beacon

    private final boolean usesGateway;

    DeviceModel() {
        this(false);
    }
    /**
     * @param usesGateway indicates whether or not given device uses gateway.
     * I.e. is beacon
     */
    DeviceModel(final boolean usesGateway) {
        this.usesGateway = usesGateway;
    }

    /**
     * @return the isBeacon
     */
    public boolean isUseGateway() {
        return usesGateway;
    }
}
