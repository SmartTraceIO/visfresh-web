/**
 *
 */
package com.visfresh.tools;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.visfresh.logs.LogUnit;
import com.visfresh.model.DeviceMessage;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class UnexpectedMessageTypeSamplesCollector extends MessageExtractor implements ExtractedMessageHandler {
    private Map<String, String> samples = new HashMap<>();
    private Set<String> expectedTypes = new HashSet<>();

    /**
     * Default constructor.
     */
    public UnexpectedMessageTypeSamplesCollector() {
        super();

        addExtractedMessageHandler(this);

        expectedTypes.add("AUT");
        expectedTypes.add("BRT");
        expectedTypes.add("DRK");
        expectedTypes.add("INIT");
        expectedTypes.add("RSP");
        expectedTypes.add("STP");
        expectedTypes.add("VIB");
    }

    /* (non-Javadoc)
     * @see com.visfresh.tools.ExtractedMessageHandler#handle(com.visfresh.tracker.DeviceMessage)
     */
    @Override
    public void handle(final LogUnit u, final DeviceMessage m) {
        if (!expectedTypes.contains(m.getType())) {
            samples.put(m.getType(), new String(u.getRawData()));
        }
    }

    /* (non-Javadoc)
     * @see com.visfresh.tools.MessageExtractor#containsDevice(java.lang.String)
     */
    @Override
    protected boolean containsDevice(final String message) {
        if (!this.devices.isEmpty()) {
            return super.containsDevice(message);
        }
        return true;
    }

    public static void main(final String[] args) throws IOException {
        final UnexpectedMessageTypeSamplesCollector ext = new UnexpectedMessageTypeSamplesCollector();

//        final File inFile = new File("/home/soldatov/tmp/logs/visfresh-dcs-root.log");
        final File inFile = new File("/home/soldatov/tmp/logs/visfresh-dcs.log");
        ext.extractMessages(inFile);

        final List<String> types = new LinkedList<String>(ext.getSamples().keySet());
        Collections.sort(types);

        for (final String t : types) {
            System.out.println(">>> Sample of type '" + t + "':");
            System.out.println(ext.getSamples().get(t));
        }
    }
    /**
     * @return the messageTypes
     */
    public Map<String, String> getSamples() {
        return samples;
    }
}
