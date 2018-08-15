/**
 *
 */
package au.smarttrace.geolocation;

import java.util.LinkedList;
import java.util.List;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class MultiBeaconMessage {
    private String gateway;
    private DeviceMessage gatewayMessage;
    private List<DeviceMessage> beacons = new LinkedList<>();

    /**
     * Default constructor.
     */
    public MultiBeaconMessage() {
        super();
    }

    /**
     * @return the gateway
     */
    public String getGateway() {
        return gateway;
    }
    /**
     * @param gateway the gateway to set
     */
    public void setGateway(final String gateway) {
        this.gateway = gateway;
    }
    /**
     * @return the gatewayMessage
     */
    public DeviceMessage getGatewayMessage() {
        return gatewayMessage;
    }
    /**
     * @param gatewayMessage the gatewayMessage to set
     */
    public void setGatewayMessage(final DeviceMessage gatewayMessage) {
        this.gatewayMessage = gatewayMessage;
    }
    /**
     * @return the beacons
     */
    public List<DeviceMessage> getBeacons() {
        return beacons;
    }
    /**
     * @param beacons the beacons to set
     */
    public void setBeacons(final List<DeviceMessage> beacons) {
        this.beacons = beacons;
    }
}
