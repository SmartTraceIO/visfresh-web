/**
 *
 */
package com.visfresh.service;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.visfresh.Device;
import com.visfresh.DeviceCommand;
import com.visfresh.db.DeviceCommandDao;
import com.visfresh.db.DeviceDao;
import com.visfresh.db.MessageSnapshootDao;

import au.smarttrace.geolocation.DataWithGsmInfo;
import au.smarttrace.geolocation.DeviceMessage;
import au.smarttrace.geolocation.DeviceMessageType;
import au.smarttrace.geolocation.GeoLocationRequest;
import au.smarttrace.geolocation.ServiceType;
import au.smarttrace.geolocation.SingleMessageGeoLocationFacade;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class DeviceMessageService {
    private static int NUMBER_OF_MESSAGES_IN_CRITICAL_BATH = 8;
    private static final Logger log = LoggerFactory.getLogger(DeviceMessageService.class);
    public static final String SERVICE_NAME = "old-dcs";

    @Autowired
    private DeviceDao deviceDao;
    @Autowired
    private NamedParameterJdbcTemplate jdbc;
    @Autowired
    private MessageSnapshootDao snapshootDao;
    @Autowired
    private DeviceCommandDao deviceCommandDao;
    @Autowired
    private InactiveDeviceAlertSender alerter;

    protected SingleMessageGeoLocationFacade helper;

    /**
     * Default constructor.
     */
    public DeviceMessageService() {
        super();
    }

    @PostConstruct
    public void initialize() {
        helper = SingleMessageGeoLocationFacade.createFacade(jdbc, ServiceType.UnwiredLabs, SERVICE_NAME);
    }

    @Scheduled(fixedDelay = 10000l)
    public void handleResolvedLocations() {
        helper.processResolvedLocations();
    }
    /**
     * @param rqs
     * @return
     */
    public DeviceCommand process(final List<DataWithGsmInfo<DeviceMessage>> rqs, final String rawData) {
        if (rqs.size() == 0) {
            return null;
        }

        if (!saveSignature(getMessages(rqs))) {
            log.warn("The message batch is already processed, will ignored: " + rawData);
            return null;
        }

        Device device = null;
        boolean hasInitMessage = false;

        for (final DataWithGsmInfo<DeviceMessage> rq : rqs) {
            final DeviceMessage msg = rq.getUserData();
            //attempt to load device
            if (device == null) {
                device = getDeviceByImei(msg.getImei());
            }

            if (device == null) {
                log.warn("Not found registered device " + msg.getImei());
                break;
            } else if (!device.isActive()) {
                log.debug("Device " + device.getImei() + " is inactive, message(s) ignored");

                sendAlert("Attempt to send message to inactive device " + msg.getImei(),
                        "Message body:\n" + rawData);
                break;
            } else {
                if (msg.getType() == DeviceMessageType.RSP) {
                    //process response to server command
                    log.debug("Device " + msg.getImei() + " has sent command response: " + msg.getMessage());
                } else {
                    if (msg.getType() == DeviceMessageType.INIT) {
                        hasInitMessage = true;
                    }
                    saveLocationResolvingRequest(rq);
                }
            }
        }

        DeviceCommand cmd = null;
        if (device != null && rqs.size() != NUMBER_OF_MESSAGES_IN_CRITICAL_BATH) {
            final List<DeviceCommand> commands = getCommandsForDevice(device.getImei());
            if (hasInitMessage) {
                //delete all shutdown commands
                final Iterator<DeviceCommand> iter = commands.iterator();
                while (iter.hasNext()) {
                    final DeviceCommand next = iter.next();
                    if (next.getCommand().toLowerCase().contains("shutdown")) {
                        iter.remove();
                        log.debug("shutdown command has ignored because init message");
                        deleteCommand(next);
                    }
                }
            }

            if (!commands.isEmpty()) {
                if (rqs.size() != NUMBER_OF_MESSAGES_IN_CRITICAL_BATH) {
                    cmd = commands.get(0);
                    deleteCommand(cmd);
                } else {
                    log.warn("Given message batch has " + NUMBER_OF_MESSAGES_IN_CRITICAL_BATH
                            + " messages, the device command" + commands.get(0) + " will postponed");
                }
            }
        }

        return cmd;
    }
    /**
     * @param rqs
     * @return
     */
    private List<DeviceMessage> getMessages(final List<DataWithGsmInfo<DeviceMessage>> rqs) {
        final List<DeviceMessage> msgs = new LinkedList<>();
        for (final DataWithGsmInfo<DeviceMessage> rq : rqs) {
            msgs.add(rq.getUserData());
        }
        return msgs;
    }

    /**
     * @param requests list of requests.
     * @return
     */
    protected boolean saveSignature(final List<DeviceMessage> msgs) {
        return this.snapshootDao.saveSignature(msgs);
    }

    /**
     * @param subject
     * @param message
     */
    protected void sendAlert(final String subject, final String message) {
        alerter.sendAlert(new String[0], subject, message);
    }
    /**
     * @param msg the device message.
     */
    protected void saveLocationResolvingRequest(final DataWithGsmInfo<DeviceMessage> info) {
        final GeoLocationRequest req = helper.createRequest(info);
        helper.saveRequest(req);
    }
    /**
     * @param cmd the device command.
     */
    protected void deleteCommand(final DeviceCommand cmd) {
        deviceCommandDao.delete(cmd);
    }
    /**
     * @param imei device IMEI.
     * @return
     */
    protected List<DeviceCommand> getCommandsForDevice(final String imei) {
        return deviceCommandDao.getFoDevice(imei);
    }
    /**
     * @param imei device IMEI.
     * @return device.
     */
    protected Device getDeviceByImei(final String imei) {
        return deviceDao.getByImei(imei);
    }
}
