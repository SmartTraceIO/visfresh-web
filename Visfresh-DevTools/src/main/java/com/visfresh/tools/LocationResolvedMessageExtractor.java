/**
 *
 */
package com.visfresh.tools;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.visfresh.logs.AbstractVisfreshLogParser;
import com.visfresh.logs.LogUnit;
import com.visfresh.model.DeviceMessage;
import com.visfresh.model.Location;
import com.visfresh.tracker.DeviceMessageParser;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class LocationResolvedMessageExtractor extends AbstractVisfreshLogParser {
    private final List<ExtractedMessageHandler> handlers = new LinkedList<>();
    private DeviceMessageParser parser = new DeviceMessageParser();
    private final Map<String, List<DeviceMessage>> messageCache = new HashMap<>();

    /**
     * Default constructor.
     */
    public LocationResolvedMessageExtractor() {
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

        if (logLocation.contains("DeviceCommunicationServlet")) {
            //do parse
            parser.setParseDate(true);
            handleIncommingMessage(u);
        } else if (logLocation.contains("DeviceMessageDispatcher")) {
            parser.setParseDate(false);
            handleLocation(u);
        }
    }
    /**
     * @param u log unit.
     */
    private void handleLocation(final LogUnit u) {
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
                    final List<DeviceMessage> list = messageCache.get(m.getImei());
                    if (list != null && list.size() > 0) {
                        final DeviceMessage msg = list.remove(0);
                        msg.setLocation(loc);
                        fireMessageExtracted(u, msg);
                    }
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
     * @param u log unit.
     */
    private void handleIncommingMessage(final LogUnit u) {
        final String text = u.getMessage();
        final String prefix = "device message has received: ";
        final int index = text.indexOf(prefix);

        if (index > -1) {
            final List<DeviceMessage> ms = parser.parse(text.substring(index + prefix.length()));

            //add logging time
            for (final DeviceMessage m: ms) {
                m.setLoggTime(u.getDate());

                List<DeviceMessage> list = messageCache.get(m.getImei());
                if (list == null) {
                    list = new LinkedList<>();
                    messageCache.put(m.getImei(), list);
                }

                list.add(m);
            }
        }
    }
    /**
     * @param u log unit.
     * @param m message.
     */
    private void fireMessageExtracted(final LogUnit u, final DeviceMessage m) {
        for (final ExtractedMessageHandler h : this.handlers) {
            h.handle(u, m);
        }
    }
    public void addExtractedMessageHandler(final ExtractedMessageHandler h) {
        handlers.add(h);
    }
    public void deleteExtractedMessageHandler(final ExtractedMessageHandler h) {
        handlers.remove(h);
    }
}
