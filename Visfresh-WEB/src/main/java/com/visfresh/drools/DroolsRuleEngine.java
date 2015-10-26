/**
 *
 */
package com.visfresh.drools;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.kie.api.KieServices;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.JsonElement;
import com.visfresh.dao.DeviceDao;
import com.visfresh.dao.TrackerEventDao;
import com.visfresh.entities.Device;
import com.visfresh.entities.SystemMessage;
import com.visfresh.entities.SystemMessageType;
import com.visfresh.entities.TrackerEvent;
import com.visfresh.io.JSonSerializer;
import com.visfresh.mpl.services.DeviceDcsNativeEvent;
import com.visfresh.mpl.services.SystemMessageDispatcher;
import com.visfresh.services.RetryableException;
import com.visfresh.services.RuleEngine;
import com.visfresh.services.SystemMessageHandler;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class DroolsRuleEngine implements RuleEngine, SystemMessageHandler {
    private static final Logger log = LoggerFactory.getLogger(DroolsRuleEngine.class);

    @Autowired
    private SystemMessageDispatcher dispatcher;
    @Autowired
    private TrackerEventDao eventDao;
    @Autowired
    private JSonSerializer jsonSerializer;
    @Autowired
    private TrackerEventDao trackerEventDao;
    @Autowired
    private DeviceDao deviceDao;

    private KieContainer kie;

    private final Map<String, TrackerEventRule> rules = new ConcurrentHashMap<String, TrackerEventRule>();
    /**
     * @param env
     */
    public DroolsRuleEngine() {
        super();
    }

    @PostConstruct
    public void initialize() {
        final KieServices ks = KieServices.Factory.get();
        this.kie = ks.getKieClasspathContainer();
        //create first session for initialize compilation
        final KieSession session = kie.newKieSession("ksession-rules");
        session.setGlobal("engine", this);
        session.destroy();

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
        final TrackerEvent e = new TrackerEvent();
        e.setBattery(event.getBattery());
        e.setLatitude(event.getLocation().getLatitude());
        e.setLongitude(event.getLocation().getLongitude());
        e.setTemperature(event.getBattery());
        e.setTime(event.getTime());
        e.setType(event.getType());
        final String imei = event.getImei();

        final Device device = deviceDao.findAllByImei(imei).get(0);
        e.setDevice(device);

        //process tracker event with rule engine.
        processTrackerEvent(e);
    }

    @Override
    public void processTrackerEvent(final TrackerEvent e) {
        log.debug("Tracker event has received " + e);
        final KieSession session = kie.newKieSession("ksession-rules");

        try {
            session.setGlobal("engine", this);
            session.insert(new TrackerEventRequest(e));
            session.fireAllRules();
        } finally {
            session.destroy();
        }

        trackerEventDao.save(e);
    }

    /**
     * @param name rule name.
     * @return rule.
     */
    public TrackerEventRule getRule(final String name) {
        TrackerEventRule rule = rules.get(name);
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
