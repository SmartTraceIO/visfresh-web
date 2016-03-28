/**
 *
 */
package com.visfresh.rules;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.JsonElement;
import com.visfresh.dao.DeviceDao;
import com.visfresh.dao.ShipmentDao;
import com.visfresh.dao.TrackerEventDao;
import com.visfresh.entities.AlertProfile;
import com.visfresh.entities.AlertRule;
import com.visfresh.entities.Location;
import com.visfresh.entities.Shipment;
import com.visfresh.entities.SystemMessage;
import com.visfresh.entities.TemperatureRule;
import com.visfresh.entities.TrackerEvent;
import com.visfresh.entities.TrackerEventType;
import com.visfresh.io.json.DeviceDcsNativeEventSerializer;
import com.visfresh.mpl.services.DeviceDcsNativeEvent;
import com.visfresh.mpl.services.TrackerMessageDispatcher;
import com.visfresh.rules.state.DeviceState;
import com.visfresh.services.RetryableException;
import com.visfresh.services.RuleEngine;
import com.visfresh.services.SystemMessageHandler;
import com.visfresh.utils.LocationUtils;
import com.visfresh.utils.SerializerUtils;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public abstract class AbstractRuleEngine implements RuleEngine, SystemMessageHandler {
    private static final Logger log = LoggerFactory.getLogger(AbstractRuleEngine.class);
    private static final int TIME_ZONE_OFSET = TimeZone.getDefault().getRawOffset();

    @Autowired
    private TrackerMessageDispatcher dispatcher;
    @Autowired
    private ShipmentDao shipmentDao;
    @Autowired
    private TrackerEventDao trackerEventDao;
    @Autowired
    private DeviceDao deviceDao;

    private final DeviceDcsNativeEventSerializer deviceEventParser = new DeviceDcsNativeEventSerializer();
    private final Map<String, TrackerEventRule> rules = new ConcurrentHashMap<String, TrackerEventRule>();

    private final TrackerEventRule emptyRule = new TrackerEventRule() {
        @Override
        public boolean handle(final RuleContext e) {
            return false;
        }
        @Override
        public boolean accept(final RuleContext e) {
            return false;
        }
    };

    /**
     *
     */
    public AbstractRuleEngine() {
        super();
    }

    @PostConstruct
    public void initialize() {
        dispatcher.setHandler(this);
    }
    @PreDestroy
    public void shutdown() {
        dispatcher.setHandler(null);
    }

    @Override
    public void handle(final SystemMessage msg) throws RetryableException {
        final JsonElement e = SerializerUtils.parseJson(msg.getMessageInfo());
        log.debug("Native DCS event has received " + e);

        final DeviceDcsNativeEvent event = deviceEventParser.parseDeviceDcsNativeEvent(
                e.getAsJsonObject());
        //convert the UTC time to local
        event.setDate(new Date(event.getTime().getTime() + TIME_ZONE_OFSET));

        processDcsEvent(event);
    }
    /**
     * @param event
     */
    public void processDcsEvent(final DeviceDcsNativeEvent event) throws RetryableException {
        final TrackerEvent e = new TrackerEvent();
        e.setBattery(event.getBattery());
        e.setLatitude(event.getLocation().getLatitude());
        e.setLongitude(event.getLocation().getLongitude());
        e.setTemperature(event.getTemperature());
        e.setTime(event.getTime());
        e.setType(TrackerEventType.valueOf(event.getType()));

        final String imei = event.getImei();

        //set device
        e.setDevice(deviceDao.findByImei(imei));
        trackerEventDao.save(e);
        log.debug("Tracker event accepted: " + e);

        //process tracker event with rule engine.
        DeviceState state = deviceDao.getState(imei);
        if (state == null) {
            state = new DeviceState();
        } else if (state.getLastLocation() != null) {
            final Location loc = state.getLastLocation();
            final int meters = (int) LocationUtils.getDistanceMeters(loc.getLatitude(), loc.getLongitude(),
                    e.getLatitude(), e.getLongitude());
            if (meters > 200000) {
                log.warn("Incorrect device moving to " + meters + " meters has detected. Event has ignored");
                return;
            }
        }

        final RuleContext context = new RuleContext(e, state);

        try {
            invokeRules(context);

            //update last shipment date.
            final Shipment shipment = e.getShipment();
            if (shipment != null) {
                shipment.setLastEventDate(e.getTime());
                shipmentDao.save(shipment);
                log.debug("Last shipment date of " + shipment.getShipmentDescription()
                        + " has updated to " + shipment.getLastEventDate());
            }
        } finally {
            state.setLastLocation(new Location(e.getLatitude(), e.getLongitude()));
            deviceDao.saveState(event.getImei(), state);
        }
    }
    /**
     * @param name rule name.
     * @return rule.
     */
    @Override
    public TrackerEventRule getRule(final String name) {
        final TrackerEventRule rule = rules.get(name);
        if (name == null) {
            log.warn("Rule with name " + name + " is not found. Given drools expression will ignored");
            return emptyRule;
        }
        return rule;
    }
    /**
     * @param name rule name.
     * @param rule rule.
     */
    protected void setRule(final String name, final TrackerEventRule rule) {
        if (rule == null) {
            rules.remove(name);
        } else {
            rules.put(name, rule);
        }
    }
    /* (non-Javadoc)
     * @see com.visfresh.services.RuleEngine#getAlertYetFoFire(com.visfresh.entities.Shipment)
     */
    @Override
    public List<AlertRule> getAlertYetFoFire(final Shipment s) {
        final String imei = s.getDevice().getImei();
        final List<AlertRule> alerts = new LinkedList<AlertRule>();

        //check alert profile exists
        final AlertProfile alertProfile = s.getAlertProfile();
        if (alertProfile == null) {
            return alerts;
        }

        //check this shipment is active
        final Shipment lastShipment = shipmentDao.findLastShipment(imei);
        if (!lastShipment.getId().equals(s.getId())) {
            return alerts;
        }

        //check device state is set.
        final DeviceState state = deviceDao.getState(imei);
        for (final TemperatureRule rule: alertProfile.getAlertRules()) {
            switch (rule.getType()) {
                case Cold:
                case CriticalCold:
                case Hot:
                case CriticalHot:
                    if (state == null || !isTemperatureRuleProcessed(state, rule)) {
                        alerts.add(rule);
                    }
                    break;
                    default:
                        //nothing
            }
        }

        return alerts;
    }
    /**
     * @param rule
     * @return
     */
    protected static String createProcessedKey(final TemperatureRule rule) {
        return TemperatureAlertRule.NAME + "_" + rule.getType() + "_" + rule.getId() + "_processed";
    }
    /**
     * @param state device state.
     * @param rule alert rule.
     * @return
     */
    protected static boolean isTemperatureRuleProcessed(final DeviceState state,
            final TemperatureRule rule) {
        return "true".equals(state.getTemperatureAlerts().getProperties().get(createProcessedKey(rule)));
    }
    /**
     * @param deviceState
     * @param rule
     */
    protected static void setProcessedTemperatureRule(final DeviceState deviceState, final TemperatureRule rule) {
        deviceState.getTemperatureAlerts().getProperties().put(createProcessedKey(rule), "true");
    }
}
