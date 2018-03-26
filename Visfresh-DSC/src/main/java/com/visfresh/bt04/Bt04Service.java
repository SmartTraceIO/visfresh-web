/**
 *
 */
package com.visfresh.bt04;

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
        final Device device = getDeviceByImei(msgs.getImei());

        if (device == null) {
            log.warn("Not found registered device " + msgs.getImei());
            return;
        } else if (!device.isActive()) {
            log.debug("Device " + device.getImei() + " is inactive, message(s) ignored");

            sendAlert("Attempt to send message to inactive device " + device.getImei(),
                    "Message body:\n" + msgs.getRawData());
            return;
        }

        for (final Beacon b : msgs.getBeacons()) {
            final DeviceMessage msg = createFromHeader(msgs);

            msg.setBattery(convertToBatteryLevel(b.getBattery()));
            msg.setBeaconId(b.getSn());
            msg.setTime(b.getLastScannedTime());
            msg.setType(DeviceMessageType.AUT);
            msg.setTemperature(b.getTemperature());

            Location loc = null;
            if (msgs.getLatitude() != null && msgs.getLongitude() != null) {
                loc = new Location(msgs.getLatitude(), msgs.getLongitude());
            }

            sendMessage(msg, loc);
        }
    }
    /**
     * @param battery in percents.
     * @return compatible battery level.
     */
    private int convertToBatteryLevel(final double battery) {
        return (int) Math.round(4070.0 * battery);
    }
    /**
     * @param msgs BT04 message batch.
     * @return
     */
    private DeviceMessage createFromHeader(final Bt04Message msgs) {
        final DeviceMessage m = new DeviceMessage();
        m.setImei(msgs.getImei());
        return m;
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
