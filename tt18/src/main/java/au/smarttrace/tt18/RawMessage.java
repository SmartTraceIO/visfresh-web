/**
 *
 */
package au.smarttrace.tt18;

import java.util.Date;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class RawMessage {
    /**
     * Company identifier.
     */
    private String company;
    /**
     * The packet length.
     */
    private int packetLength;
    /**
     * Protocol number.
     */
    private String protocolNumber;
    /**
     * Hardware type.
     */
    private String hardwareType;
    /**
     * Hardware version.
     */
    private String hardwareVersion;
    /**
     * IMEI code.
     */
    private String imei;
    /**
     * Message time.
     */
    private Date time;
    /**
     * Local area code.
     */
    private int lac;
    /**
     * CELL ID.
     */
    private int cellId;
    /**
     * Mobile country code.
     */
    private int mcc;
    /**
     * Mobile network code.
     */
    private int mnc;
    private Alarm alarm;
    private boolean rtcTimeAbnormal;
    private boolean timeHumiditySensorAbnormal;
    private boolean temparatureHumidityOverTreshold;
    private boolean batteryLow;
    private boolean charging;
    private int signalLevel;
    private boolean internetConnectionEstablished;
    private boolean gprsIsRegistered;
    private boolean inRoaming;
    private boolean gsmIsRegistered;
    private boolean simCardDetected;
    private boolean gsmModuleStarted;
    /**
     * Battery level in mB
     */
    private int battery;
    private int packetIndex;
    private Double temperature;

    /**
     * @param companyIdentifier
     */
    public void setCompany(final String companyIdentifier) {
        this.company = companyIdentifier;
    }
    /**
     * @return the company
     */
    public String getCompany() {
        return company;
    }
    /**
     * @return the packetLength
     */
    public int getPacketLength() {
        return packetLength;
    }
    /**
     * @param packetLength the packetLength to set
     */
    public void setPacketLength(final int packetLength) {
        this.packetLength = packetLength;
    }
    /**
     * @param num the protocol number.
     */
    public void setProtocolNumber(final String num) {
        this.protocolNumber = num;
    }
    /**
     * @return the protocolNumber
     */
    public String getProtocolNumber() {
        return protocolNumber;
    }
    /**
     * @param type hardware type.
     */
    public void setHardwareType(final String type) {
        this.hardwareType = type;
    }
    /**
     * @return the hardwareType
     */
    public String getHardwareType() {
        return hardwareType;
    }
    /**
     * @param version
     */
    public void setHardwareVersion(final String version) {
        this.hardwareVersion = version;
    }
    /**
     * @return the hardwareVersion
     */
    public String getHardwareVersion() {
        return hardwareVersion;
    }
    /**
     * @param imei IMEI code.
     */
    public void setImei(final String imei) {
        this.imei = imei;
    }
    /**
     * @return the imei
     */
    public String getImei() {
        return imei;
    }
    /**
     * @param d time to set.
     */
    public void setTime(final Date d) {
        this.time = d;
    }
    /**
     * @return the time
     */
    public Date getTime() {
        return time;
    }
    /**
     * @param lac local area code.
     */
    public void setLac(final int lac) {
        this.lac = lac;
    }
    /**
     * @return the lac
     */
    public int getLac() {
        return lac;
    }
    /**
     * @param id CELL ID.
     */
    public void setCellId(final int id) {
        this.cellId = id;
    }
    /**
     * @return the cellId
     */
    public int getCellId() {
        return cellId;
    }
    /**
     * @param mcc mobile country code.
     */
    public void setMcc(final int mcc) {
        this.mcc = mcc;
    }
    /**
     * @return the mcc
     */
    public int getMcc() {
        return mcc;
    }
    /**
     * @param mnc mobile network code.
     */
    public void setMnc(final int mnc) {
        this.mnc = mnc;
    }
    /**
     * @return the mnc
     */
    public int getMnc() {
        return mnc;
    }
    /**
     * @param a alarm type.
     */
    public void setAlarm(final Alarm a) {
        alarm = a;
    }
    /**
     * @return the alarm
     */
    public Alarm getAlarm() {
        return alarm;
    }
    /**
     * @param b
     */
    public void setRtcTimeAbnormal(final boolean b) {
        rtcTimeAbnormal = b;
    }
    /**
     * @return the rtcTimeAbnormal
     */
    public boolean isRtcTimeAbnormal() {
        return rtcTimeAbnormal;
    }
    /**
     * @param b
     */
    public void setTimeHumiditySensorAbnormal(final boolean b) {
        timeHumiditySensorAbnormal = b;
    }
    /**
     * @return the timeHumiditySensorAbnormal
     */
    public boolean isTimeHumiditySensorAbnormal() {
        return timeHumiditySensorAbnormal;
    }
    /**
     * @param b
     */
    public void setTemparatureHumidityOverTreshold(final boolean b) {
        temparatureHumidityOverTreshold = b;
    }
    /**
     * @return the temparatureHumidityOverTreshold
     */
    public boolean isTemparatureHumidityOverTreshold() {
        return temparatureHumidityOverTreshold;
    }
    /**
     * @param b
     */
    public void setBatteryLow(final boolean b) {
        batteryLow = b;
    }
    /**
     * @return the batteryLow
     */
    public boolean isBatteryLow() {
        return batteryLow;
    }
    /**
     * @param b
     */
    public void setCharging(final boolean b) {
        charging = b;
    }
    /**
     * @return the charging
     */
    public boolean isCharging() {
        return charging;
    }
    /**
     * @param value
     */
    public void setSignalLevel(final int value) {
        signalLevel = value;
    }
    /**
     * @return the signalLevel
     */
    public int getSignalLevel() {
        return signalLevel;
    }
    /**
     * @param b
     */
    public void setInternetConnectionEstablished(final boolean b) {
        internetConnectionEstablished = b;
    }
    /**
     * @return the internetConnectionEstablished
     */
    public boolean isInternetConnectionEstablished() {
        return internetConnectionEstablished;
    }
    /**
     * @param b
     */
    public void setGprsIsRegistered(final boolean b) {
        gprsIsRegistered = b;
    }
    /**
     * @return the gprsIsRegistered
     */
    public boolean isGprsIsRegistered() {
        return gprsIsRegistered;
    }
    /**
     * @param b
     */
    public void setInRoaming(final boolean b) {
        inRoaming = b;
    }
    /**
     * @return the inRoaming
     */
    public boolean isInRoaming() {
        return inRoaming;
    }
    /**
     * @param b
     */
    public void setGsmIsRegistered(final boolean b) {
        gsmIsRegistered = b;
    }
    /**
     * @return the gsmIsRegistered
     */
    public boolean isGsmIsRegistered() {
        return gsmIsRegistered;
    }
    /**
     * @param b
     */
    public void setSimCardDetected(final boolean b) {
        simCardDetected = b;
    }
    /**
     * @return the simCardDetected
     */
    public boolean isSimCardDetected() {
        return simCardDetected;
    }
    /**
     * @param b
     */
    public void setGsmModuleStarted(final boolean b) {
        gsmModuleStarted = b;
    }
    /**
     * @return the gsmModuleStarted
     */
    public boolean isGsmModuleStarted() {
        return gsmModuleStarted;
    }
    /**
     * @param value
     */
    public void setBattery(final int value) {
        battery = value;
    }
    /**
     * @return the battery
     */
    public int getBattery() {
        return battery;
    }
    /**
     * @param value
     */
    public void setPacketIndex(final int value) {
        packetIndex = value;
    }
    /**
     * @return the packetIndex
     */
    public int getPacketIndex() {
        return packetIndex;
    }
    /**
     * @param value
     */
    public void setTemperature(final Double value) {
        temperature = value;
    }
    /**
     * @return the temperature
     */
    public Double getTemperature() {
        return temperature;
    }
}
