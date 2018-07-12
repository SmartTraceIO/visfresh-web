/**
 *
 */
package au.smarttrace.eel.rawdata;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class WiFiStationSignal {
    //BSSID 6 WiFi MAC address
    private String bssid;
    //RSSI 1 WiFi signal level --- Signed 8 bits integer (in dB)
    private int rssi;

    /**
     * Default constructor.
     */
    public WiFiStationSignal() {
        super();
    }

    /**
     * @return the bssid
     */
    public String getBssid() {
        return bssid;
    }
    /**
     * @param bssid the bssid to set
     */
    public void setBssid(final String bssid) {
        this.bssid = bssid;
    }
    /**
     * @return the rssi
     */
    public int getRssi() {
        return rssi;
    }
    /**
     * @param rssi the rssi to set
     */
    public void setRssi(final int rssi) {
        this.rssi = rssi;
    }
}
