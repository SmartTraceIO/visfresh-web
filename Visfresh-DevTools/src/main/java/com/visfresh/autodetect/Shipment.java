/**
 *
 */
package com.visfresh.autodetect;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class Shipment {
    private long id;
    private String device;
    private int tripCount;

    /**
     * Default constructor.
     */
    public Shipment() {
        super();
    }

    /**
     * @return the id
     */
    public long getId() {
        return id;
    }
    /**
     * @param id the id to set
     */
    public void setId(final long id) {
        this.id = id;
    }
    /**
     * @return the sn
     */
    public String getSn() {
        return getSerialNumber(getDevice());
    }
    /**
     * @param device device
     */
    public void setDevice(final String device) {
        this.device = device;
    }
    /**
     * @return the device
     */
    public String getDevice() {
        return device;
    }
    /**
     * @return the tripCount
     */
    public int getTripCount() {
        return tripCount;
    }
    /**
     * @param tripCount the tripCount to set
     */
    public void setTripCount(final int tripCount) {
        this.tripCount = tripCount;
    }
    /**
     * @param imei device IMEI.
     * @return
     */
    public static String getSerialNumber(final String imei) {
        if (imei == null) {
            return null;
        }

        //normalize device serial number
        final int len = imei.length();
        final StringBuilder sb = new StringBuilder(imei.substring(len - 7, len - 1));
        while (sb.charAt(0) == '0' && sb.length() > 1) {
            sb.deleteCharAt(0);
        }

        return sb.toString();
    }
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return getId() + " (sn: " + getSn() + ", trip: " + getTripCount() + ")";
    }
}
