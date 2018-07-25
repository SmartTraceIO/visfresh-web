/**
 *
 */
package com.visfresh.dispatcher;

import com.visfresh.DeviceMessage;
import com.visfresh.DeviceMessageParser;
import com.visfresh.Location;
import com.visfresh.RadioType;
import com.visfresh.StationSignal;

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
        //{
        //    "token": "a93bb2c1f9699a",
        //    "radio": "lte",
        //    "mcc": 505,
        //    "mnc": 1,
        //    "cells": [{
        //        "lac": 65534,
        //        "cid": 133237004,
        //        "psc": 0
        //    }],
        //    "address": 1
        //}
        final DeviceMessage msg = parseDeviceMessage();
        msg.setImei("352544074664971");
        msg.setRadio(RadioType.lte);
        final StationSignal sig = new StationSignal();
//        MCC: 505, MNC: 1, LAC: 65534, CID: 133237005, RX Level: 15
        sig.setMcc(505);
        sig.setMnc(1);
        sig.setCi(133237005);
        sig.setLac(65534);
        sig.setLevel(15);
        msg.getStations().add(sig);

        final UnwiredLabsLocationService svc = createClient();
        final Location location = svc.getLocation(msg.getImei(),
                msg.getRadio() == null ? null : msg.getRadio().name(), msg.getStations());
        System.out.println(location);
    }

    /**
     * @return
     */
    protected static UnwiredLabsLocationService createClient() {
        final UnwiredLabsLocationService svc = new UnwiredLabsLocationService() {};
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
