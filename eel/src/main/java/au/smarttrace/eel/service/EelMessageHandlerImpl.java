/**
 *
 */
package au.smarttrace.eel.service;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

import javax.annotation.PostConstruct;

import org.apache.commons.codec.binary.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import au.smarttrace.eel.Beacon;
import au.smarttrace.eel.DeviceMessage;
import au.smarttrace.eel.GatewayBinding;
import au.smarttrace.eel.db.BeaconDao;
import au.smarttrace.eel.db.SystemMessageDao;
import au.smarttrace.eel.rawdata.BeaconData;
import au.smarttrace.eel.rawdata.DevicePosition;
import au.smarttrace.eel.rawdata.EelMessage;
import au.smarttrace.eel.rawdata.EelPackage;
import au.smarttrace.eel.rawdata.GsmStationSignal;
import au.smarttrace.eel.rawdata.LocationPackageBody;
import au.smarttrace.geolocation.GeoLocationRequest;
import au.smarttrace.geolocation.Location;
import au.smarttrace.geolocation.RequestStatus;
import au.smarttrace.geolocation.impl.RetryableEvent;
import au.smarttrace.gsm.GsmLocationResolvingRequest;
import au.smarttrace.gsm.StationSignal;
import au.smarttrace.json.ObjectMapperFactory;
import au.smarttrace.unwiredlabs.UnwiredLabsHelper;
import au.smarttrace.unwiredlabs.UnwiredLabsService;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class EelMessageHandlerImpl implements EelMessageHandler {
    public static final double COORDINATES_MASHTAB = 1800000.;
    public static final String SENDER = "eel";
    private static final Logger log = LoggerFactory.getLogger(EelMessageHandlerImpl.class);

    @Autowired
    private SystemMessageDao messageDao;
    @Autowired
    private BeaconDao deviceDao;
    @Autowired
    private InactiveDeviceAlertSender alerter;
    @Autowired
    private BeaconChannelLockService lockService;
    @Autowired
    private NamedParameterJdbcTemplate jdbc;
    private UnwiredLabsHelper unwiredLabsHelper;
    private final ObjectMapper json = ObjectMapperFactory.craeteObjectMapper();
    /**
     * Default constructor.
     */
    public EelMessageHandlerImpl() {
        super();
    }

    @PostConstruct
    public void initialize() {
        unwiredLabsHelper = UnwiredLabsService.createHelper(jdbc);
    }

    @Scheduled(fixedDelay = 10000l)
    public void handleResolvedLocations() {
        final List<RetryableEvent> requests = getProcessedRequests(SENDER);
        for (final RetryableEvent e : requests) {
            final GeoLocationRequest req = e.getRequest();
            try {
                if (req.getStatus() == RequestStatus.success) {
                    try {
                        final LocationDetectRequest r = json.readValue(
                                req.getUserData(), LocationDetectRequest.class);
                        final Location loc = json.readValue(req.getBuffer(), Location.class);

                        //set location to messages
                        for (final DeviceMessage m : r.getMessages()) {
                            m.setLocation(loc);
                        }

                        sendResolvedMessages(r.getImei(), r.getMessages());
                    } catch (final IOException exc) {
                        log.error("Failed to send resolved message to system", exc);
                    }
                }
            } finally {
                deleteEvent(e);
            }
        }
    }
    /**
     * @param e
     */
    private void deleteEvent(final RetryableEvent e) {
        unwiredLabsHelper.deleteRequest(e);

    }

    /* (non-Javadoc)
     * @see au.smarttrace.eel.service.EelMessageHandler#handleMessage(au.smarttrace.eel.rawdata.EelMessage)
     */
    @Override
    public void handleMessage(final EelMessage eelMessage) {
        log.debug("Message for " + eelMessage.getImei()
            + " has received. Packages: " + eelMessage.getPackages());
        log.debug("Raw data:\n" + encodeHexString(eelMessage.getRawData()));

        final Map<String, Boolean> beaconCache = new HashMap<>();
        final List<DeviceMessage> toHandle = new LinkedList<>();
        final List<LocationDetectRequest> toLoctionDetect = new LinkedList<>();
        final boolean isGatewayRegistered = isTrackerRegistered(eelMessage.getImei());

        for (final EelPackage pck : eelMessage.getPackages()) {
            if (pck.getBody() instanceof LocationPackageBody) {
                final LocationPackageBody locBody = (LocationPackageBody) pck.getBody();
                final DevicePosition location = locBody.getLocation();
                if (!hasLocation(location)) {
                    log.debug("Location package for " + eelMessage.getImei()
                        + " has not location data, will ignored");
                    continue;
                }

                final Date time = fromEpoch(location.getTime());
                Location loc = null;
                List<DeviceMessage> msgs;
                if (location.getGpsData() != null) { //create location.
                    loc = new Location(
                        location.getGpsData().getLatitude() / COORDINATES_MASHTAB,
                        location.getGpsData().getLongitude() / COORDINATES_MASHTAB);
                    msgs = toHandle;
                } else { //if not location, create location detection request
                    //and collect messages to supply by given request.
                    final LocationDetectRequest req = new LocationDetectRequest();
                    req.setImei(eelMessage.getImei());
                    req.setReq(createRequest(eelMessage.getImei(), location));
                    toLoctionDetect.add(req);

                    msgs = req.getMessages();
                }

                if (isGatewayRegistered) {
                    final DeviceMessage msg = createDeviceMessage(eelMessage, locBody);
                    msg.setLocation(loc);
                    msg.setTime(time);

                    msgs.add(msg);
                }

                for (final BeaconData bs : locBody.getBeacons()) {
                    final String beaconImei = bs.getAddress();

                    final boolean shouldHandleBeacon = shouldHandleBeacon(
                            beaconImei, eelMessage, beaconCache);
                    if (shouldHandleBeacon) {
                        final DeviceMessage msg = createDeviceMessage(bs);
                        msg.setGateway(eelMessage.getImei());
                        msg.setTime(time);
                        msg.setLocation(loc);

                        msgs.add(msg);
                    } else {
                        log.warn("Not found registered device " + beaconImei
                                + " for beacon " + bs.getAddress() + " message will ignored");
                    }
                }
            }
        }

        if (!toHandle.isEmpty()) {
            sendResolvedMessages(eelMessage.getImei(), toHandle);
        }
        if (!toLoctionDetect.isEmpty()) {
            for (final LocationDetectRequest req : toLoctionDetect) {
                sendLocationResolvingRequest(req);
            }
        }
    }

    /**
     * @param locBody location package body.
     * @return device message.
     */
    private DeviceMessage createDeviceMessage(final EelMessage eel, final LocationPackageBody locBody) {
        final DeviceMessage msg = new DeviceMessage();
        msg.setImei(eel.getImei());
        msg.setHumidity(locBody.getHumidity() / 10);
        //Battery 2 Battery voltage (in mV,(Battery * (3.6 / 4095.0) )) --- Unsigned 16 bits integer
        msg.setBattery(locBody.getBattery());
        //Temperature 2 Sensor temperature (in (1/256)C) --- Unsigned 16 bits integer
        msg.setTemperature(locBody.getTemperature() / 256.);
        return msg;
    }

    /**
     * @param req
     */
    private void sendLocationResolvingRequest(final LocationDetectRequest req) {
        if (req.getMessages().size() == 0) {
            //ignore request without messages.
            return;
        }

        final GsmLocationResolvingRequest r = req.getReq();
        req.setReq(null);
        try {
            final String userData = json.writeValueAsString(req);
            saveUnwiredLabsRequest(SENDER, userData, r);
        } catch (final JsonProcessingException e) {
            log.error("Failed to send location resolving request for " + req.getReq().getImei(), e);
        }
    }
    /**
     * @param imei
     * @param location
     * @return
     */
    private GsmLocationResolvingRequest createRequest(final String imei, final DevicePosition location) {
        final GsmLocationResolvingRequest req = new GsmLocationResolvingRequest();
        req.setImei(imei);
        for (final GsmStationSignal s : location.getTowerSignals()) {
            final StationSignal sig = createStationSignal(s);
            req.getStations().add(sig);

            final boolean isLte = (sig.getCi() >> 16) > 0;
            req.setRadio(isLte ? "lte" : "gsm");
        }
        return req;
    }

    /**
     * @param gateway
     * @param toHandle
     */
    public void sendResolvedMessages(final String gateway, final List<DeviceMessage> toHandle) {
        final Set<String> beacons = lockBeaconChannels(getOnlyBeaconImeis(toHandle), gateway);
        for (final DeviceMessage m : toHandle) {
            if (m.getGateway() == null || beacons.contains(m.getImei())) {
                sendMessage(m);
            } else {
                log.debug("Beacon " + m.getImei() + " channel has locked by another gateway");
            }
        }
    }

    /**
     * @param beaconImei
     * @param eelMessage
     * @param beaconCache
     * @return
     */
    protected boolean shouldHandleBeacon(final String beaconImei, final EelMessage eelMessage,
            final Map<String, Boolean> beaconCache) {
        boolean shouldHandleBeacon = false;
        if (beaconCache.containsKey(beaconImei)) {
            shouldHandleBeacon = beaconCache.get(beaconImei) == Boolean.TRUE;
        } else {
            final Beacon beacon = getBeaconByImei(beaconImei);
            shouldHandleBeacon = beacon != null && beacon.isActive();

            //only send warning at first found time
            if (beacon != null) {
                if (!beacon.isActive()) {
                    log.debug("Beacon " + beaconImei + " is inactive, message ignored");
                    sendAlert("Attempt to send message to inactive device " + beaconImei,
                            "Message body:\n" + rawDataToHexString(eelMessage));
                    shouldHandleBeacon = false;
                } else if (!hasValidGateway(beacon)) {
                    log.debug("Beacon " + beaconImei
                            + " has invalid or inactive gateway, message ignored");
                    sendAlert("Attempt to send message to device with invalid or inactive gateway"
                            + beaconImei, "Message body:\n" + rawDataToHexString(eelMessage));
                    shouldHandleBeacon = false;
                } else if (!deviceIsGateway(eelMessage.getImei(), beacon)) {
                    log.debug("Beacon " + beaconImei
                            + " has gateway " + beacon.getGateway().getGateway()
                            + ", but attempted to send using " + eelMessage.getImei());
                    shouldHandleBeacon = false;
                }

                beaconCache.put(beaconImei, shouldHandleBeacon ? Boolean.TRUE : Boolean.FALSE);
            }
        }
        return shouldHandleBeacon;
    }

    /**
     * @param rawData
     * @return
     */
    private String encodeHexString(final byte[] rawData) {
        if (rawData != null) {
            return Hex.encodeHexString(rawData);
        }
        return null;
    }

    /**
     * @param bs
     * @param location
     * @return
     */
    protected DeviceMessage createDeviceMessage(final BeaconData bs) {
        final DeviceMessage msg = new DeviceMessage();
        msg.setImei(bs.getAddress());
        //Battery 2 Battery voltage (in mV,(Battery * (3.6 / 4095.0) )) --- Unsigned 16 bits integer
        msg.setBattery(Math.round(bs.getBattery()));
        //Temperature 2 Sensor temperature (in (1/256)C) --- Unsigned 16 bits integer
        msg.setTemperature(bs.getTemperature() / 256.);
        return msg;
    }
    /**
     * @param s
     * @return
     */
    private StationSignal createStationSignal(final GsmStationSignal s) {
        final StationSignal sig = new StationSignal();
        sig.setCi(s.getCid());
        sig.setLac(s.getLac());
        sig.setLevel(s.getRxLevel());
        sig.setMcc(s.getMcc());
        sig.setMnc(s.getMnc());
        return sig;
    }
    /**
     * @param eelMessage
     * @return
     */
    protected String rawDataToHexString(final EelMessage eelMessage) {
        return Hex.encodeHexString(eelMessage.getRawData());
    }
    /**
     * @param location
     * @return
     */
    private boolean hasLocation(final DevicePosition location) {
        return location.getGpsData() != null
                || !location.getTowerSignals().isEmpty()
                //TODO uncomment when WiFi support will added
//                || !location.getWiFiSignals().isEmpty()
                ;
    }
    /**
     * @param sender
     * @return
     */
    protected List<RetryableEvent> getProcessedRequests(final String sender) {
        return unwiredLabsHelper.getProcessedRequests(sender);
    }
    /**
     * @param sender
     * @param userData
     * @param r
     */
    protected void saveUnwiredLabsRequest(final String sender, final String userData, final GsmLocationResolvingRequest r) {
        unwiredLabsHelper.saveRequest(sender, userData, r);
    }
    /**
     * @param beacons set of beacon IMEI.
     * @param gateway TODO
     * @return successfully locked channels.
     */
    protected Set<String> lockBeaconChannels(final Set<String> beacons, final String gateway) {
        return lockService.lockChannels(beacons, gateway);
    }

    /**
     * @param msgs messages.
     * @return set of IMEI.
     */
    private Set<String> getOnlyBeaconImeis(final List<DeviceMessage> msgs) {
        final Set<String> imeis = new HashSet<>();
        for (final DeviceMessage m : msgs) {
            if (m.getGateway() != null) {
                imeis.add(m.getImei());
            }
        }
        return imeis;
    }

    /**
     * @param imei
     * @param beacon
     * @return
     */
    private boolean deviceIsGateway(final String imei, final Beacon beacon) {
        final GatewayBinding gateway = beacon.getGateway();
        return gateway == null || imei.equals(gateway.getGateway());
    }

    /**
     * @param b
     * @return
     */
    private boolean hasValidGateway(final Beacon b) {
        final GatewayBinding gateway = b.getGateway();
        if (gateway != null) {
            if (!gateway.isActive() || !gateway.getCompany().equals(b.getCompany())) {
                return false;
            }
        }
        return true;
    }
    /**
     * @param t
     * @return
     */
    private Date fromEpoch(final long t) {
        return new Date(t * 1000l - TimeZone.getDefault().getOffset(t));
    }
    /**
     * @param msg device message.
     */
    protected void sendMessage(final DeviceMessage msg) {
        log.debug("Message for " + msg.getImei() + " have resolved location");
        messageDao.sendSystemMessageFor(msg);
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
    protected Beacon getBeaconByImei(final String imei) {
        return deviceDao.getBeaconById(imei);
    }
    /**
     * @param imei
     * @return
     */
    protected boolean isTrackerRegistered(final String imei) {
        return deviceDao.isTrackerRegistered(imei);
    }
}
