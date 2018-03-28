/**
 *
 */
package com.visfresh.bt04;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.visfresh.Device;
import com.visfresh.DeviceMessage;
import com.visfresh.DeviceMessageType;
import com.visfresh.Location;
import com.visfresh.db.DeviceDao;
import com.visfresh.db.SystemMessageDao;
import com.visfresh.service.InactiveDeviceAlertSender;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class Bt04Service {
    public static final double BATTERY_FULL = 50000.0;
    public static final String IMEI_PREFIX = "bt04-";
    public static final String IMEI_SUFFIX = "x";

    private static final Logger log = LoggerFactory.getLogger(Bt04Message.class);

    @Autowired
    private SystemMessageDao messageDao;
    @Autowired
    private DeviceDao deviceDao;
    @Autowired
    private InactiveDeviceAlertSender alerter;

    /**
     * Default constructor.
     */
    public Bt04Service() {
        super();
    }

    /**
     * @param msgs
     */
    public void process(final Bt04Message msgs) {
        final Map<String, Boolean> deviceCache = new HashMap<>();

        for (final Beacon b : msgs.getBeacons()) {
            final String beaconImei = createBt04Imei(b.getSn());

            boolean foundDevice = false;
            if (deviceCache.containsKey(beaconImei)) {
                foundDevice = deviceCache.get(beaconImei) == Boolean.TRUE;
            } else {
                final Device d = getDeviceByImei(beaconImei);
                foundDevice = d != null && d.isActive();
                deviceCache.put(beaconImei, foundDevice ? Boolean.TRUE : Boolean.FALSE);

                //only send warning at first found time
                if (d != null && !d.isActive()) {
                    log.debug("Device " + beaconImei + " is inactive, message(s) ignored");
                    sendAlert("Attempt to send message to inactive device " + beaconImei,
                            "Message body:\n" + msgs.getRawData());
                }
            }

            if (foundDevice) {
                final DeviceMessage msg = new DeviceMessage();
                msg.setImei(beaconImei);
                msg.setBattery(convertToBatteryLevel(b.getBattery()));
                // msg.setBeaconId(b.getSn());
                msg.setTime(b.getLastScannedTime());
                msg.setType(DeviceMessageType.AUT);
                msg.setTemperature(b.getTemperature());

                Location loc = null;
                if (msgs.getLatitude() != null && msgs.getLongitude() != null) {
                    loc = new Location(msgs.getLatitude(), msgs.getLongitude());
                }

                sendMessage(msg, loc);
            } else {
                log.warn("Not found registered device " + beaconImei
                        + " for beacon " + b.getSn() + " message will ignored");
            }
        }
    }
    /**
     * @param sn
     * @return
     */
    public static String createBt04Imei(final String sn) {
        final StringBuilder sb = new StringBuilder(sn);
        sb.append(IMEI_SUFFIX);

        //add zero symbols
        int len = 11 - sb.length() - IMEI_PREFIX.length();
        while (len > 0) {
            sb.insert(0, '0');
            len--;
        }

        //add prefix
        sb.insert(0, IMEI_PREFIX);
        return sb.toString();
    }

    /**
     * @param battery in percents.
     * @return compatible battery level.
     */
    private int convertToBatteryLevel(final double battery) {
        return (int) Math.round(BATTERY_FULL / 100. * battery);
    }
    /**
     * @param msg device message.
     * @param loc location.
     */
    protected void sendMessage(final DeviceMessage msg, final Location loc) {
        messageDao.sendSystemMessageFor(msg, loc);
    }
    /**
     * @param subject
     * @param message
     */
    protected void sendAlert(final String subject, final String message) {
        alerter.sendAlert(new String[0], subject, message);
    }

    /**
     * @param imei device IMEI.
     * @return device.
     */
    protected Device getDeviceByImei(final String imei) {
        return deviceDao.getByImei(imei);
    }
}
