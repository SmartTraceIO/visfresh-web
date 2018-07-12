/**
 *
 */
package au.smarttrace.eel.rawdata;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class BeaconData {
    //Address 6 The Becaon device Bluetooth address (in big endian)
    private String address;
    //The Becaon device type(0xF2(STF2 device), 0xB1(STB1 device)). --- Unsigned 8 bits integer
    private byte tppe;
    //Bluetooth signal level --- Signed 8 bits integer (in dB)
    private int rssi;
    //Battery 2 Battery voltage (in mV,(Battery * (3.6 / 4095.0) )) --- Unsigned 16 bits integer
    private int battery;
    //Temperature 2 Sensor temperature (in (1/256)C) --- Unsigned 16 bits integer
    private int temperature;

    /**
     * Default constructor.
     */
    public BeaconData() {
        super();
    }

    /**
     * @return the address
     */
    public String getAddress() {
        return address;
    }
    /**
     * @param address the address to set
     */
    public void setAddress(final String address) {
        this.address = address;
    }
    /**
     * @return the tppe
     */
    public byte getTppe() {
        return tppe;
    }
    /**
     * @param tppe the tppe to set
     */
    public void setTppe(final byte tppe) {
        this.tppe = tppe;
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
    /**
     * @return the battery
     */
    public int getBattery() {
        return battery;
    }
    /**
     * @param battery the battery to set
     */
    public void setBattery(final int battery) {
        this.battery = battery;
    }
    /**
     * @return the temperature
     */
    public int getTemperature() {
        return temperature;
    }
    /**
     * @param temperature the temperature to set
     */
    public void setTemperature(final int temperature) {
        this.temperature = temperature;
    }
}
