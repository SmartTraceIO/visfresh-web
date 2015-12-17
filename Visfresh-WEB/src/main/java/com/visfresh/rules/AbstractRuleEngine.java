/**
 *
 */
package com.visfresh.rules;

import java.util.Date;
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
import com.visfresh.dao.TrackerEventDao;
import com.visfresh.entities.SystemMessage;
import com.visfresh.entities.TrackerEvent;
import com.visfresh.entities.TrackerEventType;
import com.visfresh.io.json.DeviceDcsNativeEventSerializer;
import com.visfresh.mpl.services.DeviceDcsNativeEvent;
import com.visfresh.mpl.services.TrackerMessageDispatcher;
import com.visfresh.rules.state.DeviceState;
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
    private static final int TIME_ZONE_OFSET = TimeZone.getDefault().getRawOffset();

    @Autowired
    private TrackerMessageDispatcher dispatcher;
    @Autowired
    private TrackerEventDao eventDao;
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
        }
        final RuleContext context = new RuleContext(e, state);

        try {
            invokeRules(context);
        } finally {
            //update history.
            state.addToHistory(new TemperaturePoint(e.getTemperature(), e.getTime()));
            deviceDao.saveState(imei, state);
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
        rules.put(name, rule);
    }
}
