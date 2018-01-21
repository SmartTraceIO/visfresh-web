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
}
