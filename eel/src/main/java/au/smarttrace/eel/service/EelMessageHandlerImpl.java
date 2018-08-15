/**
 *
 */
package au.smarttrace.eel.service;

import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import javax.annotation.PostConstruct;

import org.apache.commons.codec.binary.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import au.smarttrace.eel.Beacon;
import au.smarttrace.eel.GatewayBinding;
import au.smarttrace.eel.db.BeaconDao;
import au.smarttrace.eel.rawdata.BeaconData;
import au.smarttrace.eel.rawdata.DevicePosition;
import au.smarttrace.eel.rawdata.EelMessage;
import au.smarttrace.eel.rawdata.EelPackage;
import au.smarttrace.eel.rawdata.GsmStationSignal;
import au.smarttrace.eel.rawdata.LocationPackageBody;
import au.smarttrace.geolocation.DataWithGsmInfo;
import au.smarttrace.geolocation.DeviceMessage;
import au.smarttrace.geolocation.GeoLocationRequest;
import au.smarttrace.geolocation.GeoLocationResponse;
import au.smarttrace.geolocation.Location;
import au.smarttrace.geolocation.MultiBeaconGeoLocationFacade;
import au.smarttrace.geolocation.MultiBeaconMessage;
import au.smarttrace.geolocation.RequestStatus;
import au.smarttrace.geolocation.ServiceType;
import au.smarttrace.gsm.GsmLocationResolvingRequest;
import au.smarttrace.gsm.StationSignal;

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
    private BeaconDao deviceDao;
    @Autowired
    private InactiveDeviceAlertSender alerter;
    @Autowired
    private NamedParameterJdbcTemplate jdbc;
    private MultiBeaconGeoLocationFacade facade;
    /**
     * Default constructor.
     */
    public EelMessageHandlerImpl() {
        super();
    }

    @PostConstruct
    public void initialize() {
        facade = MultiBeaconGeoLocationFacade.createFacade(jdbc, ServiceType.UnwiredLabs, SENDER);
    }

    @Scheduled(fixedDelay = 10000l)
    public void handleResolvedLocations() {
        facade.processResolvedLocations();
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
        final List<DataWithGsmInfo<MultiBeaconMessage>> toHandle = new LinkedList<>();
        final List<DataWithGsmInfo<MultiBeaconMessage>> toLoctionDetect = new LinkedList<>();
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

                Location loc = null;
                final Date time = fromEpoch(location.getTime());

                final DataWithGsmInfo<MultiBeaconMessage> ldr = new DataWithGsmInfo<>();
                ldr.setUserData(new MultiBeaconMessage());
                ldr.getUserData().setGateway(eelMessage.getImei());

                if (location.getGpsData() != null) { //create location.
                    loc = new Location(
                        location.getGpsData().getLatitude() / COORDINATES_MASHTAB,
                        location.getGpsData().getLongitude() / COORDINATES_MASHTAB);
                    toHandle.add(ldr);
                } else { //if not location, create location detection request
                    //and collect messages to supply by given request.
                    ldr.setGsmInfo(createRequest(eelMessage.getImei(), location));
                    toLoctionDetect.add(ldr);
                }

                if (isGatewayRegistered) {
                    final DeviceMessage msg = createDeviceMessage(eelMessage, locBody);
                    msg.setLocation(loc);
                    msg.setTime(time);

                    ldr.getUserData().setGatewayMessage(msg);
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

                        ldr.getUserData().getBeacons().add(msg);
                    } else {
                        log.warn("Not found registered device " + beaconImei
                                + " for beacon " + bs.getAddress() + " message will ignored");
                    }
                }
            }
        }

        //process resolved messages.
        if (!toHandle.isEmpty()) {
            for (final DataWithGsmInfo<MultiBeaconMessage> req : toHandle) {
                processResolvedMessage(req.getUserData());
            }
        }
        //send location detect requests.
        if (!toLoctionDetect.isEmpty()) {
            for (final DataWithGsmInfo<MultiBeaconMessage> req : toLoctionDetect) {
                sendLocationResolvingRequest(req);
            }
        }
    }

    /**
     * @param req
     */
    protected void processResolvedMessage(final MultiBeaconMessage req) {
        facade.processResolvedMessage(req, RequestStatus.success);
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
    protected void sendLocationResolvingRequest(final DataWithGsmInfo<MultiBeaconMessage> req) {
        if (req.getUserData().getGatewayMessage() == null && req.getUserData().getBeacons().size() == 0) {
            //ignore request without messages.
            return;
        }

        final GeoLocationRequest r = facade.createRequest(req);
        facade.saveRequest(r);
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
    protected List<GeoLocationResponse> getAndRemoveProcessedResponses(final String sender, final int limit) {
        return facade.getAndRemoveProcessedResponses(limit);
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
