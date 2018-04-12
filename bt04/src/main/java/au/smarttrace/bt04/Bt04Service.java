/**
 *
 */
package au.smarttrace.bt04;

import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import au.smarttrace.Beacon;
import au.smarttrace.DeviceMessage;
import au.smarttrace.GatewayBinding;
import au.smarttrace.Location;
import au.smarttrace.db.BeaconDao;
import au.smarttrace.db.SystemMessageDao;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class Bt04Service {
    public static final double BATTERY_FULL = 50000.0;
    private static final String IMEI_PREFIX = "bt04-";
    private static final String IMEI_SUFFIX = "x";

    private static final Logger log = LoggerFactory.getLogger(Bt04Service.class);

    @Autowired
    private SystemMessageDao messageDao;
    @Autowired
    private BeaconDao deviceDao;
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

        for (final BeaconSignal bs : msgs.getBeacons()) {
            final String beaconImei = bs.getSn();

            boolean foundDevice = false;
            if (deviceCache.containsKey(beaconImei)) {
                foundDevice = deviceCache.get(beaconImei) == Boolean.TRUE;
            } else {
                final Beacon beacon = getDeviceByImei(beaconImei);
                foundDevice = beacon != null && beacon.isActive();

                //only send warning at first found time
                if (beacon != null) {
                    if (!beacon.isActive()) {
                        log.debug("Beacon " + beaconImei + " is inactive, message ignored");
                        sendAlert("Attempt to send message to inactive device " + beaconImei,
                                "Message body:\n" + msgs.getRawData());
                        foundDevice = false;
                    } else if (!hasValidGateway(beacon)) {
                        log.debug("Beacon " + beaconImei
                                + " has invalid or inactive gateway, message ignored");
                        sendAlert("Attempt to send message to device with invalid or inactive gateway"
                                + beaconImei, "Message body:\n" + msgs.getRawData());
                        foundDevice = false;
                    } else if (!deviceIsGateway(msgs.getImei(), beacon)) {
                        log.debug("Beacon " + beaconImei
                                + " has gateway " + beacon.getGateway().getGateway()
                                + ", but attempted to send using " + msgs.getImei());
                        foundDevice = false;
                    }

                    deviceCache.put(beaconImei, foundDevice ? Boolean.TRUE : Boolean.FALSE);
                }
            }

            if (foundDevice) {
                final DeviceMessage msg = new DeviceMessage();
                msg.setImei(beaconImei);
                msg.setBattery(convertToBatteryLevel(bs.getBattery()));
                // msg.setBeaconId(b.getSn());
                msg.setTime(bs.getLastScannedTime());
                msg.setTemperature(bs.getTemperature());

                Location loc = null;
                if (msgs.getLatitude() != null && msgs.getLongitude() != null) {
                    loc = new Location(msgs.getLatitude(), msgs.getLongitude());
                }

                sendMessage(msg, loc);
            } else {
                log.warn("Not found registered device " + beaconImei
                        + " for beacon " + bs.getSn() + " message will ignored");
            }
        }
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
     * @param sn
     * @return
     */
    private static String createBt04Imei(final String sn) {
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
    protected Beacon getDeviceByImei(final String imei) {
        return deviceDao.getById(imei);
    }

    public static void main(final String[] args) {
        final String[] colors = {
            "Black",
            "Blue",
            "BlueViolet",
            "Brown",
            "DarkBlue",
            "DarkCyan",
            "DarkGoldenrod",
            "DarkGreen",
            "DarkHhaki",
            "DarkMagenta",
            "DarkOlivegreen",
            "DarkOrange",
            "DarkOrchid",
            "DarkRed",
            "DarkSalmon",
            "DarkTurquoise",
            "DarksLategray",
            "DimGray",
            "Gold",
            "GoldenRod",
            "Gray",
            "Green",
            "HotPink",
            "IndianRed",
            "Indigo",
            "Magenta",
            "Maroon",
            "MediumAquamarine",
            "MediumsLateBlue",
            "Navy",
            "Olive",
            "Orange",
            "PaleVioletRed",
            "Peru",
            "Purple",
            "Red",
            "RosyBrown",
            "RoyalBlue",
            "SaddleBrown",
            "Salmon",
            "SandyBrown",
            "SeaGreen",
            "Sienna",
            "SlateBlue",
            "SteelBlue",
            "Tan",
            "Teal",
            "Tomato",
            "Violet",
            "YellowGreen"
        };

        //create array of serial numbers.
        final long start = 11181950;
        final long end = 11181999;
        final long company = 3;

        final PrintStream out = System.out;
        for (long sn = start; sn <= end; sn++) {
            printCreateSql(colors, Long.toString(sn), company, out);
//            tmpPrintUpdateNameSql(Long.toString(sn), out);
        }
    }

    /**
     * @param colors
     * @param sns
     * @param out
     */
    protected static void printCreateSql(final String[] colors, final String sn,
            final long company, final PrintStream out) {
        final Random random = new Random();
        //insert ignore into devices
        final String sql = "insert ignore into devices (imei, name, description, company, active, model, color)"
                + " values ('"
                + createBt04Imei(sn)
                + "', 'B04 beacon "
                + sn
                + "', 'BT04 beacon "
                + sn
                + "', "
                + company
                + ", true, 'BT04', '"
                + colors[random.nextInt(colors.length)]
                + "');";
        out.println(sql);
    }
    /**
     * @param colors
     * @param sns
     * @param out
     */
    protected static void tmpPrintUpdateNameSql(final String sn, final PrintStream out) {
        final String name = "Primo Beacon R " + sn;
        //insert ignore into devices
        final String sql = "update devices set name = '"
                + name
                + "', description = '"
                + name
                + "', autostart = 19 where imei = '"
                + createBt04Imei(sn)
                + "';";
        out.println(sql);
    }
}
