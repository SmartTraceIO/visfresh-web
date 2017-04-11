/**
 *
 */
package com.visfresh.mpl.ruleengine;

import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import javax.mail.MessagingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Component;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.visfresh.rules.AbstractRuleEngine;
import com.visfresh.rules.RuleContext;
import com.visfresh.rules.TrackerEventRule;
import com.visfresh.services.EmailService;
import com.visfresh.utils.ExceptionUtils;
import com.visfresh.utils.SerializerUtils;
import com.visfresh.utils.StringUtils;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
@ComponentScan(basePackageClasses = {AbstractRuleEngine.class})
public class VisfreshRuleEngine extends AbstractRuleEngine {
    private static final Integer DEFAULT_PRIORITY = new Integer(0);
    private static Logger log = LoggerFactory.getLogger(VisfreshRuleEngine.class);
    @Autowired
    private EmailService emailer;
    private final Map<String, Integer> priorityMap = new ConcurrentHashMap<>();
    private volatile TrackerEventRule[] rules = {};

    /**
     * @param env
     */
    public VisfreshRuleEngine() {
        super();

        try {
            final String jsonConfig = StringUtils.getContent(VisfreshRuleEngine.class.getClassLoader().getResourceAsStream(
                    "rulepriorities.json"), "UTF-8");
            final JsonObject json = SerializerUtils.parseJson(jsonConfig).getAsJsonObject();
            for (final Entry<String, JsonElement> e: json.entrySet()) {
                this.priorityMap.put(e.getKey(), e.getValue().getAsInt());
            }
        } catch (final IOException e) {
            throw new RuntimeException("Failed to load rule priority map.", e);
        }
    }

    @Override
    public void invokeRules(final RuleContext context) {
        boolean runAgain = true;

        while (runAgain) {
            runAgain = false;
            for (final TrackerEventRule rule : rules) {
                try {
                    if (rule.accept(context)) {
                        if (rule.handle(context)) {
                            runAgain = true;
                            break;
                        }
                    }
                } catch (final Throwable e) {
                    log.error("Fatal rulle processing error", e);
                    try {
                        emailer.sendMessageToSupport("Fatal rule processing error: " + e.getMessage(),
                                ExceptionUtils.getSteackTraceAsString(e, 10) + "\n...");
                    } catch (final MessagingException e1) {
                        log.error("Failed to send message to support", e1);
                    }
                }
            }
        }
    }
    /**
     * @param ruleName rule name.
     * @param priority the rule priority. Default rule priority is 0.
     */
    public void setPriority(final String ruleName, final int priority) {
        priorityMap.put(ruleName, priority);
        reloadRules();
    }
    /* (non-Javadoc)
     * @see com.visfresh.rules.AbstractRuleEngine#setRule(java.lang.String, com.visfresh.rules.TrackerEventRule)
     */
    @Override
    protected void setRule(final String name, final TrackerEventRule rule) {
        super.setRule(name, rule);
        reloadRules();
    }
    /**
     * Reloads the rules.
     */
    private void reloadRules() {
        final List<TrackerEventRule> rules = new LinkedList<>();

        final Map<TrackerEventRule, String> ruleNameMap = new HashMap<>();

        //load
        for (final String name : getRules()) {
            final TrackerEventRule rule = getRule(name);
            if (rule != null) {
                rules.add(rule);
                ruleNameMap.put(rule, name);
            }
        }

        //sort rules
        Collections.sort(rules, new Comparator<TrackerEventRule>() {
            /* (non-Javadoc)
             * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
             */
            @Override
            public int compare(final TrackerEventRule r1, final TrackerEventRule r2) {
                final Integer p1 = getPriority(ruleNameMap.get(r1));
                final Integer p2 = getPriority(ruleNameMap.get(r2));
                //higher priority should be moved to front
                return p2.compareTo(p1);
            }
        });

        this.rules = rules.toArray(new TrackerEventRule[rules.size()]);
    }
    /**
     * @param ruleName rule name.
     * @return priority associated by given rule.
     */
    protected Integer getPriority(final String ruleName) {
        final Integer p = priorityMap.get(ruleName);
        return p == null ? DEFAULT_PRIORITY : p;
    }
}
