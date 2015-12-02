/**
 *
 */
package com.visfresh.drools;

import org.kie.api.KieServices;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Component;

import com.visfresh.rules.AbstractRuleEngine;
import com.visfresh.rules.RuleContext;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
@ComponentScan(basePackageClasses = {AbstractRuleEngine.class})
public class DroolsRuleEngine extends AbstractRuleEngine {
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
    public void invokeRules(final RuleContext context) {
        final KieSession session = kie.newKieSession("ksession-rules");

        try {
            session.setGlobal("engine", this);
            session.insert(context);
            session.fireAllRules();
        } finally {
            session.destroy();
        }
    }
}
