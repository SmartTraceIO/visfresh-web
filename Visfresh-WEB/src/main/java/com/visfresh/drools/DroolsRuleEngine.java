/**
 *
 */
package com.visfresh.drools;

import org.kie.api.KieServices;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Component;

import com.visfresh.entities.TrackerEvent;
import com.visfresh.rules.AbstractRuleEngine;
import com.visfresh.rules.RuleContext;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
@ComponentScan(basePackageClasses = {AbstractRuleEngine.class})
public class DroolsRuleEngine extends AbstractRuleEngine {
    private static final Logger log = LoggerFactory.getLogger(DroolsRuleEngine.class);
    private KieContainer kie;

    /**
     * @param env
     */
    public DroolsRuleEngine() {
        super();
    }

    @Override
    public void initialize() {
        super.initialize();

        final KieServices ks = KieServices.Factory.get();
        this.kie = ks.getKieClasspathContainer();
        //create first session for initialize compilation
        final KieSession session = kie.newKieSession("ksession-rules");
        session.setGlobal("engine", this);
        session.destroy();
    }

    @Override
    public void processTrackerEvent(final TrackerEvent e) {
        log.debug("Tracker event has received " + e);
        final KieSession session = kie.newKieSession("ksession-rules");

        try {
            session.setGlobal("engine", this);
            session.insert(new RuleContext(e));
            session.fireAllRules();
        } finally {
            session.destroy();
        }

        trackerEventDao.save(e);
    }
}
