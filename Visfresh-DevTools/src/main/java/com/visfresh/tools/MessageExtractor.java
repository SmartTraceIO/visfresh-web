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
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import com.visfresh.logs.AbstractVisfreshLogParser;
import com.visfresh.logs.LogUnit;
import com.visfresh.tracker.DeviceMessage;
import com.visfresh.tracker.DeviceMessageParser;
import com.visfresh.tracker.LocationProvider;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class MessageExtractor extends AbstractVisfreshLogParser {
    private final Set<String> devices = new HashSet<>();
    private final List<ExtractedMessageHandler> handlers = new LinkedList<>();
    private DeviceMessageParser parser = new DeviceMessageParser();
    private LocationProvider locationProvider;

    /**
     * Default constructor.
     */
    public MessageExtractor() {
        super();
    }

    /**
     * @param in input stream.
     * @throws IOException
     */
    public void extractMessages(final InputStream in) throws IOException {
        parse(in);
    }
    /**
     * @param in input stream.
     * @throws IOException
     */
    public void extractMessages(final File file) throws IOException {
        final InputStream in = new BufferedInputStream(new FileInputStream(file));
        try {
            extractMessages(in);
        } finally {
            in.close();
        }
    }

    /* (non-Javadoc)
     * @see com.visfresh.logs.AbstractVisfreshLogParser#handleNextLogUnit(com.visfresh.logs.LogUnit)
     */
    @Override
    protected void handleNextLogUnit(final LogUnit u) {
        final String logLocation = u.getLocation();

        if (containsDevice(u.getMessage())) {
            if (logLocation.contains("DeviceCommunicationServlet")) {
                parseIncommingMessage(u);
            }
        }
    }
    /**
     * @param u log unit.
     */
    private void parseIncommingMessage(final LogUnit u) {
        final String text = u.getMessage();
        final String prefix = "device message has received: ";
        final int index = text.indexOf(prefix);

        if (index > -1) {
            final List<DeviceMessage> ms = parser.parse(text.substring(index + prefix.length()));

            //add logging time
            for (final DeviceMessage m: ms) {
                m.setLoggTime(u.getDate());
                if (m.getStations().size() > 0) {
                    m.setLocation(getLocationProvider().getLocation(m.getStations()));
                }

                fireMessageExtracted(m);
            }
        }
    }
    /**
     * @param m message.
     */
    private void fireMessageExtracted(final DeviceMessage m) {
        for (final ExtractedMessageHandler h : this.handlers) {
            h.handle(m);
        }
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
    /**
     * @return the devices
     */
    public Set<String> getDevices() {
        return devices;
    }
    public void addExtractedMessageHandler(final ExtractedMessageHandler h) {
        handlers.add(h);
    }
    public void deleteExtractedMessageHandler(final ExtractedMessageHandler h) {
        handlers.remove(h);
    }
    /**
     * @param lp the locationProvider to set
     */
    public void setLocationProvider(final LocationProvider lp) {
        this.locationProvider = lp;
    }
    /**
     * @return the locationProvider
     */
    public LocationProvider getLocationProvider() {
        return locationProvider;
    }
}
