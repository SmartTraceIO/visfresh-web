/**
 *
 */
package com.visfresh.dispatcher;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import com.visfresh.DeviceMessage;
import com.visfresh.DeviceMessageType;
import com.visfresh.Location;
import com.visfresh.StationSignal;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class DeviceMessageDispatcherTest extends DeviceMessageDispatcher
    implements LocationService {
    private final Map<String, List<Location>> locations = new HashMap<>();
    private final Map<DeviceMessage, Location> systemMessages = new LinkedHashMap<>();
    private final List<DeviceMessage> messages = new LinkedList<>();

    /**
     * Default constructor.
     */
    public DeviceMessageDispatcherTest() {
        super();
        setLocationService(this);
    }

    @Test
    public void testProcessMessages() {
        final DeviceMessage msg = createMessage("098709237987123");

        this.messages.add(msg);

        //add location for given message
        final List<Location> locs = new LinkedList<>();
        this.locations.put(msg.getImei(), locs);
        locs.add(new Location(10, 10));

        assertEquals(1, processMessages());
        //check system message send
        assertEquals(1, systemMessages.size());
        assertNotNull(systemMessages.get(msg));
    }
    @Test
    public void testErrorByLocationService() {
        final DeviceMessage msg = createMessage("098709237987123");

        this.messages.add(msg);

        //add location for given message
        final List<Location> locs = new LinkedList<>();
        this.locations.put(msg.getImei(), locs);
        locs.add(new Location(0, 0));

        assertEquals(1, processMessages());
        //check system message send
        assertEquals(0, systemMessages.size());
        assertEquals(1, messages.size());
    }
    @Test
    public void testErrorBy00Location() {
        final DeviceMessage msg = createMessage("098709237987123");

        this.messages.add(msg);

        assertEquals(1, processMessages());
        //check system message send
        assertEquals(1, systemMessages.size());
        assertNull(systemMessages.get(msg));
    }

    /**
     * @param imei
     * @return
     */
    private DeviceMessage createMessage(final String imei) {
        final DeviceMessage m = new DeviceMessage();
        m.setImei(imei);
        m.setId(7L);
        m.setType(DeviceMessageType.AUT);
        return m;
    }

    /* (non-Javadoc)
     * @see com.visfresh.dispatcher.AbstractDispatcher#handleSuccess(com.visfresh.DeviceMessage)
     */
    @Override
    protected void handleSuccess(final DeviceMessage msg) {
        // nothing
    }
    /* (non-Javadoc)
     * @see com.visfresh.dispatcher.DeviceMessageDispatcher#superStopProcessingByError(com.visfresh.DeviceMessage, java.lang.Throwable)
     */
    @Override
    protected void superStopProcessingByError(final DeviceMessage msg, final Throwable error) {
        //nothing
    }
    /* (non-Javadoc)
     * @see com.visfresh.dispatcher.DeviceMessageDispatcher#getDeviceMessagesForProcess()
     */
    @Override
    protected List<DeviceMessage> getDeviceMessagesForProcess() {
        final List<DeviceMessage> result = new LinkedList<DeviceMessage>(messages);
        messages.clear();
        return result;
    }
    /* (non-Javadoc)
     * @see com.visfresh.dispatcher.DeviceMessageDispatcher#sendSystemMessageFor(com.visfresh.DeviceMessage, com.visfresh.Location)
     */
    @Override
    protected void sendSystemMessageFor(final DeviceMessage m, final Location location) {
        systemMessages.put(m, location);
    }
    /* (non-Javadoc)
     * @see com.visfresh.dispatcher.AbstractDispatcher#saveForRetry(com.visfresh.DeviceMessage)
     */
    @Override
    protected void saveForRetry(final DeviceMessage msg) {
        messages.add(msg);
    }
    /* (non-Javadoc)
     * @see com.visfresh.dispatcher.LocationService#getLocation(java.lang.String, java.util.List)
     */
    @Override
    public Location getLocation(final String imei, final List<StationSignal> stations)
            throws RetryableException {
        final List<Location> locs = locations.get(imei);
        if (locs != null && locs.size() > 0) {
            return locs.remove(locs.size() - 1);
        }
        final RetryableException exc = new RetryableException();
        exc.setCanRetry(false);
        throw exc;
    }
}
