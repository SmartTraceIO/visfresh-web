/**
 *
 */
package com.visfresh.service;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.visfresh.Device;
import com.visfresh.DeviceCommand;
import com.visfresh.DeviceMessage;
import com.visfresh.DeviceMessageType;
import com.visfresh.IncommingRequest;
import com.visfresh.db.DeviceCommandDao;
import com.visfresh.db.DeviceDao;
import com.visfresh.db.MessageSnapshootDao;
import com.visfresh.db.SystemMessageDao;

import au.smarttrace.geolocation.GeoLocationHelper;
import au.smarttrace.geolocation.GeoLocationRequest;
import au.smarttrace.geolocation.GeoLocationResponse;
import au.smarttrace.geolocation.ServiceType;
import au.smarttrace.gsm.GsmLocationResolvingRequest;
import au.smarttrace.gsm.StationSignal;
import au.smarttrace.json.ObjectMapperFactory;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class DeviceMessageService {
    private static int NUMBER_OF_MESSAGES_IN_CRITICAL_BATH = 8;
    private static final Logger log = LoggerFactory.getLogger(DeviceMessageService.class);
    public static final String SERVICE_NAME = "old-dcs";

    private final ObjectMapper json = ObjectMapperFactory.craeteObjectMapper();

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
    @Autowired
    private SystemMessageDao systemMessageDao;

    protected GeoLocationHelper helper;

    /**
     * Default constructor.
     */
    public DeviceMessageService() {
        super();
    }

    @PostConstruct
    public void initialize() {
        helper = GeoLocationHelper.createHelper(jdbc, ServiceType.UnwiredLabs);
    }

    @Scheduled(fixedDelay = 10000l)
    public void handleResolvedLocations() {
        final ObjectMapper json = ObjectMapperFactory.craeteObjectMapper();

        for (int i = 0; i < 10; i++) {
            final List<GeoLocationResponse> responses = getAndRemoveProcessedResponses(SERVICE_NAME, 20);
            if (responses.isEmpty()) {
                break;
            }
            for (final GeoLocationResponse resp : responses) {
                try {
                    final DeviceMessage msg = json.readValue(
                            resp.getUserData(), DeviceMessage.class);
                    //set location to messages
                    msg.setLocation(resp.getLocation());

                    sendResolvedMessage(msg);
                } catch (final IOException exc) {
                    log.error("Failed to send resolved message to system", exc);
                }
            }
        }
    }
    /**
     * @param msg
     */
    protected void sendResolvedMessage(final DeviceMessage msg) {
        systemMessageDao.sendSystemMessageFor(msg);

        if (msg.getLocation() != null) {
            log.debug("Message for " + msg.getImei() + " saved with resolved location");
        } else {
            log.debug("Message for " + msg.getImei() + " saved without resolved location");
        }
    }

    /**
     * @param sender
     * @return
     */
    protected List<GeoLocationResponse> getAndRemoveProcessedResponses(final String sender, final int limit) {
        return helper.getAndRemoveProcessedResponses(sender, limit);
    }
    /**
     * @param rqs
     * @return
     */
    public DeviceCommand process(final IncommingRequest rqs) {
        if (rqs.getMessages().size() == 0) {
            return null;
        }

        if (!saveSignature(rqs.getMessages())) {
            log.warn("The message batch is already processed, will ignored: " + rqs.getRawData());
            return null;
        }

        Device device = null;
        boolean hasInitMessage = false;

        for (final DeviceMessage msg : rqs.getMessages()) {
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
                        "Message body:\n" + rqs.getRawData());
                break;
            } else {
                if (msg.getType() == DeviceMessageType.RSP) {
                    //process response to server command
                    log.debug("Device " + msg.getImei() + " has sent command response: " + msg.getMessage());
                } else {
                    if (msg.getType() == DeviceMessageType.INIT) {
                        hasInitMessage = true;
                    }
                    saveLocationResolvingRequest(msg, rqs.getSignals(msg));
                }
            }
        }

        DeviceCommand cmd = null;
        if (device != null && rqs.getMessages().size() != NUMBER_OF_MESSAGES_IN_CRITICAL_BATH) {
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
                if (rqs.getMessages().size() != NUMBER_OF_MESSAGES_IN_CRITICAL_BATH) {
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
    protected void saveLocationResolvingRequest(final DeviceMessage msg, final List<StationSignal> signals) {
        final GsmLocationResolvingRequest gsm = new GsmLocationResolvingRequest();
        gsm.setImei(msg.getImei());
        gsm.setRadio("gsm");
        gsm.setStations(signals);

        try {
            final GeoLocationRequest req = helper.createRequest(
                    SERVICE_NAME, json.writeValueAsString(msg), gsm);
            helper.saveRequest(req);
        } catch (final JsonProcessingException e) {
            throw new RuntimeException(e);
        }
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
