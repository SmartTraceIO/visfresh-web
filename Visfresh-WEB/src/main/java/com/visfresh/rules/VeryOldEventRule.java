/**
 *
 */
package com.visfresh.rules;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.visfresh.dao.TrackerEventDao;
import com.visfresh.entities.TrackerEvent;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class VeryOldEventRule implements TrackerEventRule {
    /**
     * Logger.
     */
    private static final Logger log = LoggerFactory.getLogger(VeryOldEventRule.class);
    protected static long MAX_TIME_DIFF = 3 * 31 * 24 * 60 * 60 * 1000l;

    /**
     * Rule name.
     */
    public static final String NAME = "VeryOldEvent";
    @Autowired
    private AbstractRuleEngine engine;
    @Autowired
    private TrackerEventDao trackerEventDao;
    /**
     * Default constructor.
     */
    public VeryOldEventRule() {
        super();
    }

    @PostConstruct
    public void initalize() {
        engine.setRule(NAME, this);
    }

    /* (non-Javadoc)
     * @see com.visfresh.drools.TrackerEventRule#accept(com.visfresh.drools.TrackerEventRequest)
     */
    @Override
    public boolean accept(final RuleContext context) {
        final TrackerEvent e = context.getEvent();
        return e.getCreatedOn().getTime() - e.getTime().getTime() > MAX_TIME_DIFF;
    }
    /* (non-Javadoc)
     * @see com.visfresh.drools.TrackerEventRule#handle(com.visfresh.drools.TrackerEventRequest)
     */
    @Override
    public boolean handle(final RuleContext context) {
        final TrackerEvent e = context.getEvent();

        log.debug("Time of incomming for event " + e.getId()
                + " is " + e.getCreatedOn() + ". The issue time is " + e.getTime()
                + ". Too long time interval. Event processing will stopped");

        if (e.getShipment() != null) {
            e.setShipment(null);
            saveTrackerEvent(e);
        }

        context.setEventConsumed();
        return false;
    }

    /**
     * @param e tracker event.
     * @return
     */
    protected void saveTrackerEvent(final TrackerEvent e) {
        trackerEventDao.save(e);
    }
}
