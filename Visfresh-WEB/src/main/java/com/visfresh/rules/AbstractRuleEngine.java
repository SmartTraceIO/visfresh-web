/**
 *
 */
package com.visfresh.rules;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.visfresh.dao.AlternativeLocationsDao;
import com.visfresh.dao.DeviceDao;
import com.visfresh.dao.ShipmentDao;
import com.visfresh.dao.TrackerEventDao;
import com.visfresh.entities.AlertProfile;
import com.visfresh.entities.AlertRule;
import com.visfresh.entities.AlternativeLocations;
import com.visfresh.entities.Device;
import com.visfresh.entities.Location;
import com.visfresh.entities.LocationProfile;
import com.visfresh.entities.Shipment;
import com.visfresh.entities.ShipmentBase;
import com.visfresh.entities.SystemMessage;
import com.visfresh.entities.TemperatureRule;
import com.visfresh.entities.TrackerEvent;
import com.visfresh.entities.TrackerEventType;
import com.visfresh.io.json.DeviceDcsNativeEventSerializer;
import com.visfresh.io.json.InterimStopSerializer;
import com.visfresh.mpl.services.DeviceDcsNativeEvent;
import com.visfresh.mpl.services.TrackerMessageDispatcher;
import com.visfresh.rules.state.DeviceState;
import com.visfresh.rules.state.ShipmentSession;
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

    @Autowired
    private TrackerMessageDispatcher dispatcher;
    @Autowired
    private ShipmentDao shipmentDao;
    @Autowired
    private TrackerEventDao trackerEventDao;
    @Autowired
    private DeviceDao deviceDao;
    @Autowired
    private AlternativeLocationsDao altLocDao;
    @Autowired
    protected EngineShipmentSessionManager sessionManager;

    private final DeviceDcsNativeEventSerializer deviceEventParser = new DeviceDcsNativeEventSerializer();
    private final InterimStopSerializer interimSerializer = new InterimStopSerializer();
    private final Map<String, TrackerEventRule> rules = new ConcurrentHashMap<>();

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

        processDcsEvent(event);
    }
    /**
     * @param event
     */
    public void processDcsEvent(final DeviceDcsNativeEvent event) throws RetryableException {
        final TrackerEvent e = new TrackerEvent();
        e.setBattery(event.getBattery());
        if (event.getLocation() != null) {
            e.setLatitude(event.getLocation().getLatitude());
            e.setLongitude(event.getLocation().getLongitude());
        }
        e.setTemperature(event.getTemperature());
        e.setTime(event.getDate());
        e.setCreatedOn(event.getCreatedOn());
        e.setType(TrackerEventType.valueOf(event.getType()));

        final String imei = event.getImei();
        //set device
        e.setDevice(findDevice(imei));

        //process tracker event with rule engine.
        DeviceState state = getDeviceState(imei);
        if (state == null) {
            state = new DeviceState();
        }

        final RuleContext context = new RuleContext(e, sessionManager);
        context.setDeviceState(state);

        //check correct moving
        if (state.getLastLocation() != null && state.getLastReadTime() != null
                && e.getLatitude() != null && e.getLongitude() != null) {
            final Location loc = state.getLastLocation();
            final int meters = (int) LocationUtils.getDistanceMeters(loc.getLatitude(), loc.getLongitude(),
                    e.getLatitude(), e.getLongitude());

            final long dt = System.currentTimeMillis() - state.getLastReadTime().getTime();
            if (meters > 200000 && dt < 30 * 60 * 1000l) {
                log.warn("Incorrect device moving to " + meters + " meters has detected. Event has ignored");
                return;
            }
        }

        saveTrackerEvent(e);
        log.debug("Tracker event accepted: " + e);

        try {
            invokeRules(context);

            //update last shipment date.
            final Shipment shipment = e.getShipment();
            if (shipment != null) {
                shipment.setLastEventDate(e.getTime());
                saveShipment(shipment);

                log.debug("Last shipment date of " + shipment.getShipmentDescription()
                        + " has updated to " + shipment.getLastEventDate());
            }
        } finally {
            state.setLastReadTime(e.getTime());
            if (e.getLatitude() != null && e.getLongitude() != null) {
                state.setLastLocation(new Location(e.getLatitude(), e.getLongitude()));
            }
            saveDeviceState(event.getImei(), state);
            if (e.getShipment() != null) {
                sessionManager.unloadSession(e.getShipment(), true);
            }
        }
    }
    /**
     * @param name rule name.
     * @return rule.
     */
    @Override
    public final TrackerEventRule getRule(final String name) {
        final TrackerEventRule rule = getRuleImpl(name);

        //create rule wrapper.
        return new TrackerEventRule() {
            @Override
            public boolean handle(final RuleContext context) {
                return rule.handle(context);
            }
            @Override
            public boolean accept(final RuleContext context) {
                if (rule == null || context.isEventConsumed()) {
                    return false;
                }
                return rule.accept(context);
            }
        };
    }

    /**
     * @param name
     * @return
     */
    private TrackerEventRule getRuleImpl(final String name) {
        TrackerEventRule rule = rules.get(name);
        if (rule == null) {
            log.warn("Rule " + name + " is not loaded now. Waiting 3 seconds for load");
            try {
                Thread.sleep(3000);
            } catch (final InterruptedException e) {
                e.printStackTrace();
            }
            rule = rules.get(name);
        }
        if (rule == null) {
            log.error("Rule with name " + name + " is not found. Given drools expression will ignored");
            return null;
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
        return getAlerts(s, false);
    }
    @Override
    public List<AlertRule> getAlertFired(final Shipment s) {
        return getAlerts(s, true);
    }
    /**
     * @param s shipment.
     * @param onlyProcessed only processed or only not processed flag.
     * @return alerts.
     */
    private List<AlertRule> getAlerts(final Shipment s, final boolean onlyProcessed) {
        final List<AlertRule> alerts = new LinkedList<AlertRule>();

        //check alert profile exists
        final AlertProfile alertProfile = s.getAlertProfile();
        if (alertProfile == null) {
            return alerts;
        }

        //check device state is set.
        final ShipmentSession session = sessionManager.loadSessionFromDb(s);
        for (final TemperatureRule rule: alertProfile.getAlertRules()) {
            switch (rule.getType()) {
                case Cold:
                case CriticalCold:
                case Hot:
                case CriticalHot:
                    final boolean isRuleProcessed = isTemperatureRuleProcessed(session, rule);
                    if (!onlyProcessed && !isRuleProcessed && (session == null || !session.isAlertsSuppressed())) {
                        alerts.add(rule);
                    } else if (onlyProcessed && isRuleProcessed){
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
    protected static boolean isTemperatureRuleProcessed(final ShipmentSession state,
            final TemperatureRule rule) {
        if (state == null) {
            return false;
        }
        return "true".equals(state.getTemperatureAlerts().getProperties().get(createProcessedKey(rule)));
    }
    /**
     * @param deviceState
     * @param rule
     */
    public static void setProcessedTemperatureRule(final ShipmentSession deviceState, final TemperatureRule rule) {
        deviceState.getTemperatureAlerts().getProperties().put(createProcessedKey(rule), "true");
    }
    @Override
    public void setInterimLocations(final ShipmentBase base, final List<LocationProfile> stops) {
        final String key = createInterimLocationsKey();

        //add interims to alternative locations
        final AlternativeLocations v = getAlternativeLocations(base);
        v.getInterim().clear();
        v.getInterim().addAll(stops);
        saveAlternativeLocations(base, v);

        //save interim location
        final JsonArray array = new JsonArray();
        for (final LocationProfile l : stops) {
            array.add(interimSerializer.toJson(l));
        }

        if (base instanceof Shipment) {
            final Shipment s = (Shipment) base;
            final ShipmentSession state = sessionManager.loadSession(s, key);
            try {
                state.setShipmentProperty(key, array.toString());
            } finally {
                sessionManager.unloadSession(s, key, true);
            }
        }
    }
    @Override
    public List<LocationProfile> getInterimLocations(final Shipment s) {
        final String key = createInterimLocationsKey();
        final ShipmentSession state = sessionManager.loadSession(s, key);

        try {
            final String str = state.getShipmentProperty(key);
            if (str != null) {
                final List<LocationProfile> locs = new LinkedList<>();
                final JsonArray array = SerializerUtils.parseJson(str).getAsJsonArray();
                for (final JsonElement e : array) {
                    locs.add(interimSerializer.parseLocation(e.getAsJsonObject()));
                }
                return locs;
            }
        } finally {
            sessionManager.unloadSession(s, key, false);
        }

        return null;
    }
    /**
     * @return
     */
    private static String createInterimLocationsKey() {
        return "InterimStop-locations";
    }
    /**
     * @param s
     * @param v
     */
    protected void saveAlternativeLocations(final ShipmentBase s,
            final AlternativeLocations v) {
        altLocDao.save(s, v);
    }
    /**
     * @param s
     * @return
     */
    protected AlternativeLocations getAlternativeLocations(final ShipmentBase s) {
        return altLocDao.getBy(s);
    }
    /* (non-Javadoc)
     * @see com.visfresh.services.RuleEngine#supressNextAlerts(com.visfresh.entities.Shipment)
     */
    @Override
    public void suppressNextAlerts(final Shipment s) {
        final String loaderId = "suppressAlerts";
        final ShipmentSession session = sessionManager.loadSession(s, loaderId);
        try {
            session.setAlertsSuppressed(true);
        } finally {
            sessionManager.unloadSession(s, loaderId, true);
        }
    }
    /* (non-Javadoc)
     * @see com.visfresh.services.RuleEngine#getAlertsSuppressionDate(com.visfresh.entities.Shipment)
     */
    @Override
    public Date getAlertsSuppressionDate(final Shipment s) {
        final String loaderId = "alertsSuppressionDate";
        final ShipmentSession session = sessionManager.loadSession(s, loaderId);
        try {
            return session.getAlertsSuppressionDate();
        } finally {
            sessionManager.unloadSession(s, loaderId, false);
        }
    }
    /* (non-Javadoc)
     * @see com.visfresh.services.RuleEngine#isAlertsSuppressed(com.visfresh.entities.Shipment)
     */
    @Override
    public boolean isAlertsSuppressed(final Shipment s) {
        final String loaderId = "alertsSuppression";
        final ShipmentSession session = sessionManager.loadSession(s, loaderId);
        try {
            return session.isAlertsSuppressed();
        } finally {
            sessionManager.unloadSession(s, loaderId, false);
        }
    }
    /**
     * @param imei device IMEI.
     * @param state device state.
     */
    protected void saveDeviceState(final String imei, final DeviceState state) {
        deviceDao.saveState(imei, state);
    }
    /**
     * @param imei
     * @return
     */
    protected DeviceState getDeviceState(final String imei) {
        return deviceDao.getState(imei);
    }
    /**
     * @param shipment shipment.
     * @return saved shipment.
     */
    protected Shipment saveShipment(final Shipment shipment) {
        return shipmentDao.save(shipment);
    }
    /**
     * @param e tracker event.
     * @return saved tracker event.
     */
    protected TrackerEvent saveTrackerEvent(final TrackerEvent e) {
        return trackerEventDao.save(e);
    }
    /**
     * @param imei device IMEI.
     * @return device.
     */
    protected Device findDevice(final String imei) {
        return deviceDao.findByImei(imei);
    }
}
