/**
 *
 */
package com.visfresh.tools;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.visfresh.logs.AbstractVisfreshLogParser;
import com.visfresh.logs.LogUnit;
import com.visfresh.model.DeviceMessage;
import com.visfresh.model.Location;
import com.visfresh.tracker.DefaultLocationProvider;
import com.visfresh.tracker.DeviceMessageParser;
import com.visfresh.tracker.LocationProvider;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class LocationProviderBuilder extends AbstractVisfreshLogParser {
    private final Set<String> devices = new HashSet<>();
    private DeviceMessageParser parser = new DeviceMessageParser();
    private DefaultLocationProvider provider;

    /**
     * Default constructor.
     */
    public LocationProviderBuilder() {
        super();
        parser.setParseDate(false);
    }

    /**
     * @param in input stream.
     * @throws IOException
     */
    public LocationProvider build(final File file) throws IOException {
        final InputStream in = new BufferedInputStream(new FileInputStream(file));
        try {
            return build(in);
        } finally {
            in.close();
        }
    }
    /**
     * @param in input stream.
     * @throws IOException
     */
    public LocationProvider build(final InputStream in) throws IOException {
        this.provider = new DefaultLocationProvider();
        parse(in);
        return provider;
    }

    /* (non-Javadoc)
     * @see com.visfresh.logs.AbstractVisfreshLogParser#handleNextLogUnit(com.visfresh.logs.LogUnit)
     */
    @Override
    protected void handleNextLogUnit(final LogUnit u) {
        final String logLocation = u.getLocation();

        if (containsDevice(u.getMessage())) {
            if (logLocation.contains("DeviceMessageDispatcher")) {
                parseIncommingMessage(u);
            }
        }
    }
    /**
     * @param u log unit.
     */
    private void parseIncommingMessage(final LogUnit u) {
        //Location (lat: -34.755534, lon: 138.601866) has detected for message
        final String text = u.getMessage();
        final String prefix = "Location (";
        final String suffix = ") has detected for message ";

        final int start = text.indexOf(prefix);

        if (start > -1) {
            final int end = text.indexOf(suffix);
            if (end > 0) {
                final List<DeviceMessage> ms = parser.parse(text.substring(end + suffix.length()));
                final Location loc = parseLocation(text.substring(start + prefix.length(), end));

                //add logging time
                for (final DeviceMessage m: ms) {
                    provider.addLocation(m.getStations(), loc);
                }
            }
        }
    }
    /**
     * @param str
     * @return
     */
    private Location parseLocation(final String str) {
        // lat: -34.755534, lon: 138.601866
        final Location loc = new Location();

        final String[] split = str.split(", *");
        loc.setLatitude(Double.parseDouble(split[0].substring(5)));
        loc.setLongitude(Double.parseDouble(split[1].substring(5)));

        return loc;
    }

    /**
     * @param message
     * @return
     */
    private boolean containsDevice(final String message) {
        for (final String imei : devices) {
            if (message.contains(imei)) {
                return true;
            }
        }
        return false;
    }
    /**
     * @param device device IMEI.
     */
    public void addDevice(final String device) {
        devices.add(device);
    }

    public static void main(final String[] args) throws IOException {
        final String device = "354430070001426";
        final LocationProviderBuilder b = new LocationProviderBuilder();
        b.addDevice(device);

//        final File inFile = new File("/home/soldatov/tmp/logs/visfresh-dcs-root.log");
        final File inFile = new File("/home/soldatov/tmp/logs/visfresh-dcs.log");
        final LocationProvider p = b.build(inFile);
        System.out.println(p);
    }
}
