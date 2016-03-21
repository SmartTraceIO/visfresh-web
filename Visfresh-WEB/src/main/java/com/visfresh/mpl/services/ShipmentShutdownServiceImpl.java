/**
 *
 */
package com.visfresh.mpl.services;

import java.util.Date;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.JsonObject;
import com.visfresh.dao.ShipmentDao;
import com.visfresh.dao.TrackerEventDao;
import com.visfresh.entities.Device;
import com.visfresh.entities.Shipment;
import com.visfresh.entities.SystemMessage;
import com.visfresh.entities.SystemMessageType;
import com.visfresh.entities.TrackerEvent;
import com.visfresh.services.DeviceCommandService;
import com.visfresh.services.RetryableException;
import com.visfresh.services.ShipmentShutdownService;
import com.visfresh.services.SystemMessageHandler;
import com.visfresh.utils.SerializerUtils;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class ShipmentShutdownServiceImpl implements ShipmentShutdownService, SystemMessageHandler {
    private static final Logger log = LoggerFactory.getLogger(ShipmentShutdownServiceImpl.class);

    @Autowired
    protected ShipmentDao shipmentDao;
    @Autowired
    protected TrackerEventDao trackerEventDao;
    @Autowired
    private DeviceCommandService commandService;
    @Autowired
    private MainSystemMessageDispatcher systemMessageDispatcher;

    /**
     * Default constructor.
     */
    public ShipmentShutdownServiceImpl() {
        super();
    }

    /* (non-Javadoc)
     * @see com.visfresh.services.SystemMessageHandler#handle(com.visfresh.entities.SystemMessage)
     */
    @Override
    public void handle(final SystemMessage msg) throws RetryableException {
        final JsonObject json = SerializerUtils.parseJson(msg.getMessageInfo()).getAsJsonObject();
        final long shipmentId = json.get("shipment").getAsLong();
        final Shipment s = getShipment(shipmentId);
        final String imei = s.getDevice().getImei();

        if (getNextShipment(s) == null) {
            log.debug("Shutdown device " + imei + " for shipment " + s.getId());

            //send device shutdown command
            shutdownDevice(s.getDevice(), new Date());
            s.setDeviceShutdownTime(new Date());
        } else {
            log.warn("Shutting down shipment " + s.getId()
                    + " is not latest shipment for given device " + imei
                    + ". Device will not shutting down");
            //calculate start shipment date
            final TrackerEvent lastEvent = getLastEvent(s);
            if (lastEvent == null) {
                s.setDeviceShutdownTime(new Date());
            } else {
                s.setDeviceShutdownTime(lastEvent.getTime());
            }
        }

        saveShipment(s);
    }

    /**
     * @param s shipment to save
     */
    protected void saveShipment(final Shipment s) {
        shipmentDao.save(s);
    }
    /**
     * @param s
     * @return
     */
    protected TrackerEvent getLastEvent(final Shipment s) {
        return trackerEventDao.getLastEvent(s);
    }
    /**
     * @param device
     * @param date
     */
    protected void shutdownDevice(final Device device, final Date date) {
        commandService.shutdownDevice(device, new Date());
    }
    /**
     * @param s
     * @return
     */
    protected Shipment getNextShipment(final Shipment s) {
        return shipmentDao.findNextShipmentFor(s);
    }
    /**
     * @param shipmentId
     * @return
     */
    protected Shipment getShipment(final long shipmentId) {
        return shipmentDao.findOne(shipmentId);
    }

    @PostConstruct
    public void listenSystemMessage() {
        this.systemMessageDispatcher.setSystemMessageHandler(SystemMessageType.ShutdownShipment, this);
    }
    @PreDestroy
    public void destroy() {
        this.systemMessageDispatcher.setSystemMessageHandler(SystemMessageType.ShutdownShipment, null);
    }
    /**
     * @param shipment shipment.
     * @param date shutdown date.
     */
    @Override
    public void sendShipmentShutdown(final Shipment shipment, final Date date) {
        final JsonObject json = new JsonObject();
        json.addProperty("shipment", shipment.getId());

        systemMessageDispatcher.sendSystemMessage(json.toString(), SystemMessageType.ShutdownShipment, date);
    }
}
