/**
 *
 */
package com.visfresh;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import au.smarttrace.gsm.StationSignal;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class IncommingRequest {
    private Map<DeviceMessage, List<StationSignal>> messages = new LinkedHashMap<>();
    private String rawData;

    /**
     * Default constructor.
     */
    public IncommingRequest() {
        super();
    }

    /**
     * @return the message
     */
    public List<DeviceMessage> getMessages() {
        return new LinkedList<>(messages.keySet());
    }
    public List<StationSignal> getSignals(final DeviceMessage m) {
        return messages.get(m);
    }
    public void addMessage(final DeviceMessage m, final List<StationSignal> signals) {
        messages.put(m, signals);
    }
    /**
     * @return the rawData
     */
    public String getRawData() {
        return rawData;
    }
    /**
     * @param rawData the rawData to set
     */
    public void setRawData(final String rawData) {
        this.rawData = rawData;
    }

    /**
     * Clear the message data.
     */
    public void clear() {
        messages.clear();
    }
    /**
     * @param m
     */
    public void remove(final DeviceMessage m) {
        messages.remove(m);
    }
}
