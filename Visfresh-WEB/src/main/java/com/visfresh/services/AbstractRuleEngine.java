/**
 *
 */
package com.visfresh.services;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public abstract class AbstractRuleEngine implements RuleEngine {
    /**
     * Update rules thread.
     */
    private Thread thread;
    /**
     * Environment
     */
    private Environment env;

    /**
     * Default constructor.
     */
    public AbstractRuleEngine(final Environment env) {
        super();
        this.env = env;
    }

    @PostConstruct
    public synchronized void start() {
        if (isStartAsynchronously()) {
            if (thread == null) {
                thread = new Thread() {
                    /* (non-Javadoc)
                     * @see java.lang.Thread#run()
                     */
                    @Override
                    public void run() {
                        while (thread == this) {
                            updateRules();
                            synchronized (this) {
                                try {
                                    wait(getUpdateRulesTimeOut());
                                } catch (final InterruptedException e) {
                                    return;
                                }
                            }
                        }
                    }
                };
                thread.start();
            }
        }
    }
    @PreDestroy
    public synchronized void destroy() {
        if (isStartAsynchronously()) {
            final Thread t = thread;
            thread = null;
            if (t != null) {
                synchronized (t) {
                    t.notify();
                }
            }
        }
    }
    /**
     * @return the updateRulesTimeOut
     */
    public long getUpdateRulesTimeOut() {
        final String prop = env.getProperty("ruleengine.ruleUpdateTimeOut");
        return prop == null ? 15000l : Long.parseLong(prop);
    }
    /**
     * @return the startAsynchronously
     */
    protected boolean isStartAsynchronously() {
        return "true".equals(env.getProperty("ruleengine.startAsync"));
    }
}
