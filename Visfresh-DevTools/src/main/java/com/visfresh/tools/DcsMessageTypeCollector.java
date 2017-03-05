/**
 *
 */
package com.visfresh.tools;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import com.visfresh.logs.LogUnit;
import com.visfresh.model.DeviceMessage;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class DcsMessageTypeCollector extends MessageExtractor implements ExtractedMessageHandler {
    private Set<String> messageTypes = new HashSet<String>();

    /**
     * Default constructor.
     */
    public DcsMessageTypeCollector() {
        super();
        addExtractedMessageHandler(this);
    }

    /* (non-Javadoc)
     * @see com.visfresh.tools.ExtractedMessageHandler#handle(com.visfresh.tracker.DeviceMessage)
     */
    @Override
    public void handle(LogUnit u, final DeviceMessage m) {
        messageTypes.add(m.getType());
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
        final DcsMessageTypeCollector ext = new DcsMessageTypeCollector();

//        final File inFile = new File("/home/soldatov/tmp/logs/visfresh-dcs-root.log");
        final File inFile = new File("/home/soldatov/tmp/logs/visfresh-dcs.log");
        ext.extractMessages(inFile);

        final List<String> types = new LinkedList<String>(ext.getMessageTypes());
        Collections.sort(types);

        for (final String t : types) {
            System.out.println(t);
        }
    }
    /**
     * @return the messageTypes
     */
    public Set<String> getMessageTypes() {
        return messageTypes;
    }
}
