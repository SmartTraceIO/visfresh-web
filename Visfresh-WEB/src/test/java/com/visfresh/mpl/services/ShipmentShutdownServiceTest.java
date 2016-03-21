/**
 *
 */
package com.visfresh.mpl.services;

import static org.junit.Assert.assertEquals;

import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.google.gson.JsonObject;
import com.visfresh.entities.Device;
import com.visfresh.entities.Shipment;
import com.visfresh.entities.ShipmentStatus;
import com.visfresh.entities.SystemMessage;
import com.visfresh.entities.SystemMessageType;
import com.visfresh.entities.TrackerEvent;
import com.visfresh.services.RetryableException;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class ShipmentShutdownServiceTest extends ShipmentShutdownServiceImpl {
    /**
     * Shipment.
     */
    private Shipment shipment;
    private final List<Shipment> shipments = new LinkedList<>();
    private final Map<String, Date> shutdowns = new HashMap<>();
    private final List<TrackerEvent> events = new LinkedList<>();
    private final List<Shipment> savedShipments = new LinkedList<>();

    /**
     * Default constructor.
     */
    public ShipmentShutdownServiceTest() {
        super();
    }

    @Before
    public void setUp() {
        final Device device = new Device();
        device.setImei("2384570987324234");

        final Shipment s = new Shipment();
        s.setId(7l);
        s.setDevice(device);
        s.setStatus(ShipmentStatus.InProgress);
        this.shipment = s;
        shipments.add(shipment);
    }

    @Test
    public void testAcceptShipmentShutdown() throws RetryableException {
        //create shutdown shipment message
        final JsonObject json = new JsonObject();
        json.addProperty("shipment", shipment.getId());

        final SystemMessage msg = new SystemMessage();
        msg.setId(1l);
        msg.setMessageInfo(json.toString());
        msg.setRetryOn(new Date());
        msg.setTime(new Date());
        msg.setType(SystemMessageType.ShutdownShipment);

        handle(msg);

        assertEquals(1, shutdowns.size());
    }
    /**
     * @param s shipment to save
     */
    @Override
    protected void saveShipment(final Shipment s) {
        savedShipments.add(s);
    }
    /**
     * @param s
     * @return
     */
    @Override
    protected TrackerEvent getLastEvent(final Shipment s) {
        if (events.size() > 0) {
            return events.get(events.size() - 1);
        }
        return null;
    }
    /**
     * @param device
     * @param date
     */
    @Override
    protected void shutdownDevice(final Device device, final Date date) {
        shutdowns.put(device.getImei(), date);
    }
    /**
     * @param s
     * @return
     */
    @Override
    protected Shipment getNextShipment(final Shipment s) {
        final int pos = shipments.indexOf(s);
        return shipments.size() > pos + 1 ? shipments.get(pos + 1) : null;
    }
    /**
     * @param shipmentId
     * @return
     */
    @Override
    protected Shipment getShipment(final long shipmentId) {
        for (final Shipment s : shipments ) {
            if (s.getId().equals(shipmentId)) {
                return s;
            }
        }
        return null;
    }
}
