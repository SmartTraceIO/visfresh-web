/**
 *
 */
package com.visfresh;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class StationSignal {
    private int mcc;
    private int mnc;
    private int lac;
    private int ci;
    private int level;

    /**
     * Default constructor.
     */
    public StationSignal() {
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
     * @return the ci
     */
    public int getCi() {
        return ci;
    }
    /**
     * @param ci the ci to set
     */
    public void setCi(final int ci) {
        this.ci = ci;
    }
    /**
     * @return the level
     */
    public int getLevel() {
        return level;
    }
    /**
     * @param level the level to set
     */
    public void setLevel(final int level) {
        this.level = level;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        //<MCC>|<MNC>|<LAC>|<CI>|<RXLEV>|
        final StringBuilder sb = new StringBuilder();
        sb.append(getMcc()).append('|');
        sb.append(getMnc()).append('|');
        sb.append(getLac()).append('|');
        sb.append(getCi()).append('|');
        sb.append(getLevel()).append('|');
        return sb.toString();
    }
}
