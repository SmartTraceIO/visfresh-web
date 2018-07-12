/**
 *
 */
package au.smarttrace.eel.rawdata;

import java.util.LinkedList;
import java.util.List;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class DevicePosition {
    // The event time (UTC) when position data is collected
    private long time;
    // The mask to indicate which data are valid (BIT0 ~ BIT6: GPS, BSID0, BSID1, BSID2, BSS0, BSS1, BSS2)
    private byte mask;
    //The following data are related to GPS and valid only if BIT0 of mask is 1
    private GpsData gpsData;
    private final List<GsmStationSignal> towerSignals = new LinkedList<>();
    private final List<WiFiStationSignal> wiFiSignals = new LinkedList<>();
    /**
     * This field is not a part of location package. Once used for
     * correct calculate an offset after parsing of location package.
     */
    private int dataSize;

    /**
     * Default constructor.
     */
    public DevicePosition() {
        super();
    }

    /**
     * @return the time
     */
    public long getTime() {
        return time;
    }
    /**
     * @param time the time to set
     */
    public void setTime(final long time) {
        this.time = time;
    }
    /**
     * @return the mask
     */
    public byte getMask() {
        return mask;
    }
    /**
     * @param mask the mask to set
     */
    public void setMask(final byte mask) {
        this.mask = mask;
    }
    /**
     * @return the gpsData
     */
    public GpsData getGpsData() {
        return gpsData;
    }
    /**
     * @param gpsData the gpsData to set
     */
    public void setGpsData(final GpsData gpsData) {
        this.gpsData = gpsData;
    }
    /**
     * @return the towerSignals
     */
    public List<GsmStationSignal> getTowerSignals() {
        return towerSignals;
    }
    /**
     * @return the wiFiSignals
     */
    public List<WiFiStationSignal> getWiFiSignals() {
        return wiFiSignals;
    }
    /**
     * @return the dataSize
     */
    public int getDataSize() {
        return dataSize;
    }
    /**
     * @param dataSize the dataSize to set
     */
    public void setDataSize(final int dataSize) {
        this.dataSize = dataSize;
    }
}
