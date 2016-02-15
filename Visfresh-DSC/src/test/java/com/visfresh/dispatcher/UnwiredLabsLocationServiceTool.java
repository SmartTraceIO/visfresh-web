/**
 *
 */
package com.visfresh.dispatcher;

import com.visfresh.DeviceMessage;
import com.visfresh.DeviceMessageParser;
import com.visfresh.Location;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class UnwiredLabsLocationServiceTool {
    private static DeviceMessageParser parser = new DeviceMessageParser();

    /**
     * Default constructor.
     */
    public UnwiredLabsLocationServiceTool() {
        super();
    }

    public static void main(final String[] args) throws RetryableException {
        final DeviceMessage msg = parseDeviceMessage();

        final UnwiredLabsLocationService svc = createClient();
        final Location location = svc.getLocation(msg.getImei(), msg.getStations());
        System.out.println(location);
    }

    /**
     * @return
     */
    protected static UnwiredLabsLocationService createClient() {
        final UnwiredLabsLocationService svc = new UnwiredLabsLocationService();
//        unwiredlabs.url=https://ap1.unwiredlabs.com/v2/process.php
        svc.setUrl("https://ap1.unwiredlabs.com/v2/process.php");
//        unwiredlabs.token=a93bb2c1f9699a
        svc.setToken("a93bb2c1f9699a");
        return svc;
    }

    /**
     * @return the message.
     */
    private static DeviceMessage parseDeviceMessage() {
        final StringBuilder msg = new StringBuilder();
        msg.append("354430070007597|BRT|2016/02/15 06:42:05|\n");
        msg.append("3801|28.50|\n");
        msg.append("505|2|2961|14013|33|\n");
        msg.append("505|2|2961|23511|28|\n");
        msg.append("505|2|2987|57261|27|\n");
        msg.append("505|2|2961|14933|19|\n");
        msg.append("505|2|2961|14932|19|\n");

        return parser.parse(msg.toString()).get(0);
    }
}
