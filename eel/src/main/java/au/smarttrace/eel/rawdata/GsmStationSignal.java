/**
 *
 */
package au.smarttrace.eel.rawdata;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class GsmStationSignal {
    // MCC 2 Mobile Country Code --- Unsigned 16 bits integer
    private int mcc;
    //MNC 2 Mobile Network Code --- Unsigned 16 bits integer
    private int mnc;
    //LAC 2 Location Area Code --- Unsigned 16 bits integer
    private int lac;
    //CID 4 Cell ID with RNC --- Unsigned 32 bits integer
    private int cid;
    //RxLev Cell signal level --- Unsigned 8 bits integer (0: -110dB 1:-109dB 2:-108dB ...110: 0dB)
    private int rxLevel;

    /**
     * Default constructor.
     */
    public GsmStationSignal() {
        super();
    }

    /**
     * @return the mcc
     */
    public int getMcc() {
        return mcc;
    }
    /**
     * @param mcc the mcc to set
     */
    public void setMcc(final int mcc) {
        this.mcc = mcc;
    }
    /**
     * @return the mnc
     */
    public int getMnc() {
        return mnc;
    }
    /**
     * @param mnc the mnc to set
     */
    public void setMnc(final int mnc) {
        this.mnc = mnc;
    }
    /**
     * @return the lac
     */
    public int getLac() {
        return lac;
    }
    /**
     * @param lac the lac to set
     */
    public void setLac(final int lac) {
        this.lac = lac;
    }
    /**
     * @return the cid
     */
    public int getCid() {
        return cid;
    }
    /**
     * @param cid the cid to set
     */
    public void setCid(final int cid) {
        this.cid = cid;
    }
    /**
     * @return the rxLevel
     */
    public int getRxLevel() {
        return rxLevel;
    }
    /**
     * @param rxLevel the rxLevel to set
     */
    public void setRxLevel(final int rxLevel) {
        this.rxLevel = rxLevel;
    }
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("MCC: " + getMcc());
        sb.append(", MNC: " + getMnc());
        sb.append(", LAC: " + getLac());
        sb.append(", CID: " + getCid());
        sb.append(", RX Level: " + getRxLevel());
        return sb.toString();
    }
}
