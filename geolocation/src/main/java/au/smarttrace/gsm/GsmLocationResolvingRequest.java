/**
 *
 */
package au.smarttrace.gsm;

import java.util.LinkedList;
import java.util.List;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class GsmLocationResolvingRequest {
    private String radio;
    private String imei;
    private List<StationSignal> stations = new LinkedList<>();
    /**
     * Default constructor.
     */
    public GsmLocationResolvingRequest() {
        super();
    }
    /**
     * @return the radio
     */
    public String getRadio() {
        return radio;
    }
    /**
     * @param radio the radio to set
     */
    public void setRadio(final String radio) {
        this.radio = radio;
    }
    /**
     * @return the imei
     */
    public String getImei() {
        return imei;
    }
    /**
     * @param imei the imei to set
     */
    public void setImei(final String imei) {
        this.imei = imei;
    }
    /**
     * @return the stations
     */
    public List<StationSignal> getStations() {
        return stations;
    }
    /**
     * @param stations the stations to set
     */
    public void setStations(final List<StationSignal> stations) {
        this.stations = stations;
    }
}
