/**
 *
 */
package au.smarttrace.eel.service;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

import org.apache.commons.codec.binary.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import au.smarttrace.eel.Beacon;
import au.smarttrace.eel.DeviceMessage;
import au.smarttrace.eel.GatewayBinding;
import au.smarttrace.eel.Location;
import au.smarttrace.eel.StationSignal;
import au.smarttrace.eel.db.BeaconDao;
import au.smarttrace.eel.db.LocationDetectRequestDao;
import au.smarttrace.eel.db.SystemMessageDao;
import au.smarttrace.eel.rawdata.BeaconData;
import au.smarttrace.eel.rawdata.DevicePosition;
import au.smarttrace.eel.rawdata.EelMessage;
import au.smarttrace.eel.rawdata.EelPackage;
import au.smarttrace.eel.rawdata.GsmStationSignal;
import au.smarttrace.eel.rawdata.LocationPackageBody;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class EelMessageHandlerImpl implements EelMessageHandler {
    public static final double COORDINATES_MASHTAB = 1800000.;
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
    private LocationDetectRequestDao locationDetectRequestDao;
    /**
     * Default constructor.
     */
    public EelMessageHandlerImpl() {
        super();
    }

    /* (non-Javadoc)
     * @see au.smarttrace.eel.service.EelMessageHandler#handleMessage(au.smarttrace.eel.rawdata.EelMessage)
     */
    @Override
    public void handleMessage(final EelMessage eelMessage) {
        final Map<String, Boolean> beaconCache = new HashMap<>();
        final List<DeviceMessage> msgs = new LinkedList<>();

        for (final EelPackage pck : eelMessage.getPackages()) {
            if (pck.getBody() instanceof LocationPackageBody) {
                final LocationPackageBody locBody = (LocationPackageBody) pck.getBody();
                final DevicePosition location = locBody.getLocation();
                if (!hasLocation(location)) {
                    log.debug("Location package for " + eelMessage.getImei()
                        + " has not location data, will ignored");
                    continue;
                }

                for (final BeaconData bs : locBody.getBeacons()) {
                    final String beaconImei = bs.getAddress();

                    boolean foundBeacon = false;
                    if (beaconCache.containsKey(beaconImei)) {
                        foundBeacon = beaconCache.get(beaconImei) == Boolean.TRUE;
                    } else {
                        final Beacon beacon = getBeaconByImei(beaconImei);
                        foundBeacon = beacon != null && beacon.isActive();

                        //only send warning at first found time
                        if (beacon != null) {
                            if (!beacon.isActive()) {
                                log.debug("Beacon " + beaconImei + " is inactive, message ignored");
                                sendAlert("Attempt to send message to inactive device " + beaconImei,
                                        "Message body:\n" + rawDataToHexString(eelMessage));
                                foundBeacon = false;
                            } else if (!hasValidGateway(beacon)) {
                                log.debug("Beacon " + beaconImei
                                        + " has invalid or inactive gateway, message ignored");
                                sendAlert("Attempt to send message to device with invalid or inactive gateway"
                                        + beaconImei, "Message body:\n" + rawDataToHexString(eelMessage));
                                foundBeacon = false;
                            } else if (!deviceIsGateway(eelMessage.getImei(), beacon)) {
                                log.debug("Beacon " + beaconImei
                                        + " has gateway " + beacon.getGateway().getGateway()
                                        + ", but attempted to send using " + eelMessage.getImei());
                                foundBeacon = false;
                            }

                            beaconCache.put(beaconImei, foundBeacon ? Boolean.TRUE : Boolean.FALSE);
                        }
                    }

                    if (foundBeacon) {
                        final DeviceMessage msg = createDeviceMessage(bs, location);
                        msg.setGateway(eelMessage.getImei());
                        msgs.add(msg);
                    } else {
                        log.warn("Not found registered device " + beaconImei
                                + " for beacon " + bs.getAddress() + " message will ignored");
                    }
                }
            }
        }

        final Set<String> beacons = lockBeaconChannels(getImei(msgs), eelMessage.getImei());
        for (final DeviceMessage m : msgs) {
            if (beacons.contains(m.getImei())) {
                sendMessage(m);
            } else {
                log.debug("Beacon " + m.getImei() + " channel has locked by another gateway");
            }
        }
    }

    /**
     * @param bs
     * @param location
     * @return
     */
    protected DeviceMessage createDeviceMessage(final BeaconData bs, final DevicePosition location) {
        final DeviceMessage msg = new DeviceMessage();
        msg.setImei(bs.getAddress());
        //Battery 2 Battery voltage (in mV,(Battery * (3.6 / 4095.0) )) --- Unsigned 16 bits integer
        msg.setBattery(Math.round(bs.getBattery()));
        msg.setTime(fromEpoch(location.getTime()));
        //Temperature 2 Sensor temperature (in (1/256)C) --- Unsigned 16 bits integer
        msg.setTemperature(bs.getTemperature() / 256.);

        if (location.getGpsData() != null) {
            final Location loc = new Location(
                    location.getGpsData().getLatitude() / COORDINATES_MASHTAB,
                    location.getGpsData().getLongitude() / COORDINATES_MASHTAB);
            msg.setLocation(loc);
        } else if (location.getTowerSignals().size() > 0) { //detect location by station signals
            for (final GsmStationSignal s : location.getTowerSignals()) {
                msg.getStationSignals().add(createStationSignal(s));
            }
        }

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
    private Set<String> getImei(final List<DeviceMessage> msgs) {
        final Set<String> imeis = new HashSet<>();
        for (final DeviceMessage m : msgs) {
            imeis.add(m.getImei());
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
        if (msg.getLocation() != null) {
            messageDao.sendSystemMessageFor(msg);
        } else {
            locationDetectRequestDao.sendRequest(msg);
        }
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
        return deviceDao.getById(imei);
    }
}
