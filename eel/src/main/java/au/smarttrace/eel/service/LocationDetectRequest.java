/**
 *
 */
package au.smarttrace.eel.service;

import java.util.LinkedList;
import java.util.List;

import au.smarttrace.eel.DeviceMessage;
import au.smarttrace.gsm.GsmLocationResolvingRequest;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class LocationDetectRequest {
    private GsmLocationResolvingRequest req;
    private List<DeviceMessage> messages = new LinkedList<>();
    private String imei;

    /**
     * Default constructor.
     */
    public LocationDetectRequest() {
        super();
    }

    /**
     * @return the req
     */
    public GsmLocationResolvingRequest getReq() {
        return req;
    }
    /**
     * @param req the req to set
     */
    public void setReq(final GsmLocationResolvingRequest req) {
        this.req = req;
    }
    /**
     * @return the messages
     */
    public List<DeviceMessage> getMessages() {
        return messages;
    }
    /**
     * @param messages the messages to set
     */
    public void setMessages(final List<DeviceMessage> messages) {
        this.messages = messages;
    }

    /**
     * @param imei
     */
    public void setImei(final String imei) {
        this.imei = imei;
    }
    /**
     * @return the imei
     */
    public String getImei() {
        return imei;
    }
}
