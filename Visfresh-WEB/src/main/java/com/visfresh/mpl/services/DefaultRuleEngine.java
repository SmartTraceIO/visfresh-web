/**
 *
 */
package com.visfresh.mpl.services;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import com.google.gson.JsonElement;
import com.visfresh.dao.DeviceDao;
import com.visfresh.dao.ShipmentDao;
import com.visfresh.dao.TrackerEventDao;
import com.visfresh.entities.Company;
import com.visfresh.entities.Device;
import com.visfresh.entities.Location;
import com.visfresh.entities.Shipment;
import com.visfresh.entities.SystemMessage;
import com.visfresh.entities.SystemMessageType;
import com.visfresh.entities.TrackerEvent;
import com.visfresh.io.JSonSerializer;
import com.visfresh.services.AbstractRuleEngine;
import com.visfresh.services.OpenJtsFacade;
import com.visfresh.services.RetryableException;
import com.visfresh.services.RuleEngine;
import com.visfresh.services.SystemMessageHandler;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class DefaultRuleEngine extends AbstractRuleEngine implements SystemMessageHandler {
    private static final Logger log = LoggerFactory.getLogger(DefaultRuleEngine.class);

    @Autowired
    private SystemMessageDispatcher dispatcher;
    @Autowired
    private TrackerEventDao eventDao;
    @Autowired
    private RuleEngine ruleEngine;
    @Autowired
    private JSonSerializer jsonSerializer;
    @Autowired
    private ShipmentDao shipmentDao;
    @Autowired
    private DeviceDao deviceDao;
    @Autowired
    private TrackerEventDao trackerEventDao;
    @Autowired
    private OpenJtsFacade openJtsFacade;

    /**
     * @param env
     */
    @Autowired
    public DefaultRuleEngine(final Environment env) {
        super(env);
    }

    @PostConstruct
    public void initialize() {
        dispatcher.setSystemMessageHandler(SystemMessageType.Tracker, this);
    }
    @PreDestroy
    public void shutdown() {
        dispatcher.setSystemMessageHandler(SystemMessageType.Tracker, null);
    }

    /* (non-Javadoc)
     * @see com.visfresh.services.SystemMessageHandler#handle(com.visfresh.entities.SystemMessage)
     */
    @Override
    public void handle(final SystemMessage msg) throws RetryableException {
        final JsonElement e = JSonSerializer.parseJson(msg.getMessageInfo());
        log.debug("Native DCS event has received " + e);

        final DeviceDcsNativeEvent event = jsonSerializer.parseDeviceDcsNativeEvent(e.getAsJsonObject());

        processDcsEvent(event);
    }

    /* (non-Javadoc)
     * @see com.visfresh.services.RuleEngine#processTrackerEvent(com.visfresh.entities.TrackerEvent)
     */
    /**
     * @param event
     */
    public void processDcsEvent(final DeviceDcsNativeEvent event) throws RetryableException {
        final String imei = event.getImei();

        Shipment shipment = shipmentDao.findActiveShipment(imei);
        Device device = null;

        if (shipment == null) {
            //create new device
            final List<Device> devices = getAllDevices(imei);
            if (devices.size() == 0) {
                final RetryableException exc = new RetryableException("Device with given IMEI " + imei
                        + " is not registered.");
                //retry after 5 minutes
                exc.setRetryTimeOut(5 * 60 * 1000L);
                throw exc;
            }

            final Device d = new Device();
            d.setImei(imei);
            d.setId(imei.substring(imei.length() - 6, imei.length()) + "." + devices.size());

            //set from latest device
            d.setCompany(devices.get(devices.size() - 1).getCompany());

            //from default device
            final Device old = devices.get(0);
            d.setName(old.getName());
            d.setDescription(old.getDescription());

            deviceDao.save(d);
            openJtsFacade.addDevice(d);

            //create shipment.
            final Shipment def = createDefaultShipment(d.getCompany(), event.getLocation());
            def.getDevices().add(d);
            shipmentDao.save(def);

            shipment = def;
            device = d;
        } else {
            //get device with given IMEI.
            for (final Device d : shipment.getDevices()) {
                if (d.getImei().equals(imei)) {
                    device = d;
                    break;
                }
            }
        }

        //create tracker event
        final TrackerEvent e = new TrackerEvent();
        e.setDevice(device);
        e.setBattery(event.getBattery());
        e.setLatitude(event.getLocation().getLatitude());
        e.setLongitude(event.getLocation().getLongitude());
        e.setTemperature(event.getBattery());
        e.setTime(event.getTime());
        e.setType(event.getType());

        trackerEventDao.save(e);

        //process tracker event with rule engine.
        processTrackerEvent(shipment, e);
        openJtsFacade.addTrackerEvent(e);
    }

    /**
     * @param imei
     * @return
     */
    private List<Device> getAllDevices(final String imei) {
        final List<Device> all = deviceDao.findAllByImei(imei);
        //sort devices by trip number.
        Collections.sort(all, new Comparator<Device>() {
            /* (non-Javadoc)
             * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
             */
            @Override
            public int compare(final Device d1, final Device d2) {
                return getTripNum(d1).compareTo(getTripNum(d2));
            }

            private Integer getTripNum(final Device d) {
                final int index = d.getId().lastIndexOf('.');
                if (index > 0) {
                    try {
                        return Integer.parseInt(d.getId().substring(index + 1));
                    } catch (final NumberFormatException e) {
                    }
                }
                return -1;
            }
        });
        return all;
    }

    /**
     * @param company
     * @param location
     * @return
     */
    private Shipment createDefaultShipment(final Company company, final Location location) {
        final Shipment s = new Shipment();
        s.setCompany(company);
        return s;
    }

    @Override
    public void processTrackerEvent(final Shipment shipment, final TrackerEvent e) {
        log.debug("Tracker event has received " + e);

//        final Shipment shipment = findShipmentForDevice(e.get)
    }

    /* (non-Javadoc)
     * @see com.visfresh.services.AbstractRuleEngine#updateRules()
     */
    @Override
    public void updateRules() {
        // TODO Auto-generated method stub

    }
}
