/**
 *
 */
package com.visfresh.rules;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

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
import com.visfresh.dao.ShipmentSessionDao;
import com.visfresh.dao.TrackerEventDao;
import com.visfresh.entities.AlertProfile;
import com.visfresh.entities.AlertRule;
import com.visfresh.entities.AlternativeLocations;
import com.visfresh.entities.Device;
import com.visfresh.entities.Language;
import com.visfresh.entities.LocationProfile;
import com.visfresh.entities.Shipment;
import com.visfresh.entities.ShipmentBase;
import com.visfresh.entities.SystemMessage;
import com.visfresh.entities.TemperatureRule;
import com.visfresh.entities.TrackerEvent;
import com.visfresh.entities.TrackerEventType;
import com.visfresh.impl.services.DeviceDcsNativeEvent;
import com.visfresh.impl.services.TrackerMessageDispatcher;
import com.visfresh.io.json.DeviceDcsNativeEventSerializer;
import com.visfresh.io.json.InterimStopSerializer;
import com.visfresh.rules.state.DeviceState;
import com.visfresh.rules.state.ShipmentSession;
import com.visfresh.rules.state.ShipmentSessionManager;
import com.visfresh.services.RetryableException;
import com.visfresh.services.RuleEngine;
import com.visfresh.services.SystemMessageHandler;
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
    private ShipmentSessionDao shipmentSessionDao;

    private final DeviceDcsNativeEventSerializer deviceEventParser = new DeviceDcsNativeEventSerializer();
    private final InterimStopSerializer interimSerializer = new InterimStopSerializer(
            Language.English, TimeZone.getDefault());
    private final Map<String, TrackerEventRule> rules = new HashMap<>();

    protected class SessionProvider implements ShipmentSessionManager {
        private final Map<Long, ShipmentSession> sessions;

        public SessionProvider(final Map<Long, ShipmentSession> sessions) {
            this.sessions = sessions;
        }
        /* (non-Javadoc)
         * @see com.visfresh.rules.state.ShipmentSessionManager#getSession(com.visfresh.entities.Shipment)
         */
        @Override
        public ShipmentSession getSession(final Shipment s) {
            ShipmentSession session = sessions.get(s.getId());
            if (session == null) {
                session = shipmentSessionDao.getSession(s);
                if (session == null) {
                    session = new ShipmentSession(s.getId());
                    shipmentSessionDao.saveSession(session);
                }

                sessions.put(s.getId(), session);
            }
            return session;
        }
    }
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
        final TrackerEvent e = createTrackerEvent(event);

        //process tracker event with rule engine.
        DeviceState state = getDeviceState(event.getImei());
        if (state == null) {
            state = new DeviceState();
        }

        final Map<Long, ShipmentSession> sessions = new HashMap<>();
        final RuleContext context = new RuleContext(e, new SessionProvider(sessions));
        context.setDeviceState(state);

        saveTrackerEvent(e);
        log.debug("Tracker event accepted: " + e);

        try {
            invokeRules(context);

            //update last shipment date.
            final Shipment shipment = e.getShipment();
            if (shipment != null) {
                updateLastEventDate(shipment, e.getTime());

                log.debug("Last shipment date of " + shipment.getShipmentDescription()
                        + " has updated to " + shipment.getLastEventDate());
            }
        } finally {
            saveDeviceState(event.getImei(), state);
            for (final ShipmentSession session : sessions.values()) {
                shipmentSessionDao.saveSession(session);
            }
        }
    }

    /**
     * @param event
     * @return
     */
    private TrackerEvent createTrackerEvent(final DeviceDcsNativeEvent event) {
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

        //set device
        e.setDevice(findDevice(event.getImei()));
        return e;
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
            /* (non-Javadoc)
             * @see java.lang.Object#toString()
             */
            @Override
            public String toString() {
                return rule == null ? "null" : rule.toString();
            }
        };
    }

    /**
     * @param name
     * @return
     */
    protected TrackerEventRule getRuleImpl(final String name) {
        synchronized(rules) {
            return rules.get(name);
        }
    }
    /**
     * @param name rule name.
     * @param rule rule.
     */
    protected void setRule(final String name, final TrackerEventRule rule) {
        synchronized (rules) {
            if (rule == null) {
                rules.remove(name);
            } else {
                rules.put(name, rule);
            }
        }
    }
    /* (non-Javadoc)
     * @see com.visfresh.services.RuleEngine#hasRule(java.lang.String)
     */
    @Override
    public boolean hasRule(final String name) {
        synchronized (rules) {
            return name == null ? false : rules.containsKey(name);
        }
    }
    @Override
    public Set<String> getRules() {
        synchronized (rules) {
            return new HashSet<>(rules.keySet());
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
        //check alert profile exists
        final AlertProfile alertProfile = s.getAlertProfile();
        if (alertProfile == null) {
            return new LinkedList<>();
        }
        final List<TemperatureRule> alertRules = alertProfile.getAlertRules();
        final ShipmentSession session = getShipmentSession(s);

        return getFailteredAlertRules(alertRules, session, onlyProcessed);
    }

    /**
     * @param alertRules
     * @param session
     * @param onlyProcessed
     * @return
     */
    public static List<AlertRule> getFailteredAlertRules(final List<TemperatureRule> alertRules,
            final ShipmentSession session, final boolean onlyProcessed) {
        final List<AlertRule> alerts = new LinkedList<AlertRule>();
        //check device state is set.
        if (session != null) {
            for (final TemperatureRule rule: alertRules) {
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
        }

        return alerts;
    }

    /**
     * @param s
     * @return
     */
    protected ShipmentSession getShipmentSession(final Shipment s) {
        ShipmentSession session = shipmentSessionDao.getSession(s);
        if (session == null) {
            session = new ShipmentSession(s.getId());
            shipmentSessionDao.saveSession(session);
        }
        return session;
    }
    /**
     * @param rule
     * @return
     */
    protected static String createProcessedKey(final TemperatureRule rule) {
        return TemperatureAlertRule.NAME + "_" + rule.getType() + "_" + rule.getId() + "_processed";
    }
    /**
     * @param session device state.
     * @param rule alert rule.
     * @return
     */
    protected static boolean isTemperatureRuleProcessed(final ShipmentSession session,
            final TemperatureRule rule) {
        if (session == null) {
            return false;
        }
        return "true".equals(session.getTemperatureAlerts().getProperties().get(createProcessedKey(rule)));
    }
    /**
     * @param session
     * @param rule
     */
    public static void setProcessedTemperatureRule(final ShipmentSession session, final TemperatureRule rule) {
        session.getTemperatureAlerts().getProperties().put(createProcessedKey(rule), "true");
    }
    @Override
    public void updateInterimLocations(final ShipmentSession session, final List<LocationProfile> interims) {
        final String interimsKey = createInterimLocationsKey();

        //should override
        //save interim location
        final JsonArray array = new JsonArray();
        for (final LocationProfile l : interims) {
            array.add(interimSerializer.toJson(l));
        }

        session.setShipmentProperty(interimsKey, array.toString());
    }
    @Override
    public void updateAutodetectingEndLocations(final ShipmentSession session, final List<LocationProfile> to) {
        if (AutoDetectEndLocationRule.hasAutoDetectData(session)) {
            AutoDetectEndLocationRule.setAutoDetectLocations(session, to);
        }
    }
    @Override
    public List<LocationProfile> getInterimLocations(final Shipment s) {
        final String key = createInterimLocationsKey();
        final ShipmentSession session = getShipmentSession(s);

        try {
            final String str = session.getShipmentProperty(key);
            if (str != null) {
                final List<LocationProfile> locs = new LinkedList<>();
                final JsonArray array = SerializerUtils.parseJson(str).getAsJsonArray();
                for (final JsonElement e : array) {
                    locs.add(interimSerializer.parseLocationProfile(e.getAsJsonObject()));
                }
                return locs;
            }
        } finally {
            shipmentSessionDao.saveSession(session);
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
        final ShipmentSession session = getShipmentSession(s);
        try {
            session.setAlertsSuppressed(true);
        } finally {
            shipmentSessionDao.saveSession(session);
        }
    }
    /* (non-Javadoc)
     * @see com.visfresh.services.RuleEngine#getAlertsSuppressionDate(com.visfresh.entities.Shipment)
     */
    @Override
    public Date getAlertsSuppressionDate(final Shipment s) {
        final ShipmentSession session = getShipmentSession(s);
        return session.getAlertsSuppressionDate();
    }
    /* (non-Javadoc)
     * @see com.visfresh.services.RuleEngine#isAlertsSuppressed(com.visfresh.entities.Shipment)
     */
    @Override
    public boolean isAlertsSuppressed(final Shipment s) {
        final ShipmentSession session = getShipmentSession(s);
        return session.isAlertsSuppressed();
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
     * @param date last event date.
     * @return saved shipment.
     */
    protected void updateLastEventDate(final Shipment shipment, final Date date) {
        shipment.setLastEventDate(date);
        shipmentDao.updateLastEventDate(shipment, date);
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
