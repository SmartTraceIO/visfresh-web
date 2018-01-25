/**
 *
 */
package au.smarttrace.tt18.st;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public enum DeviceMessageType {
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
    //unexpected types
    BAT0,
    BAT1,
    BAT2,
    CRG0,
    CRG1,
    UNDEF
}
