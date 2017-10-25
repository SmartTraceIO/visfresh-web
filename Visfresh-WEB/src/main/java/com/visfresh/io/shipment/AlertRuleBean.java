/**
 *
 */
package com.visfresh.io.shipment;

import com.visfresh.entities.AlertRule;
import com.visfresh.entities.AlertType;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class AlertRuleBean {
    private AlertType type;
    private Long id;

    /**
     * Default constructor.
     */
    public AlertRuleBean() {
        super();
    }
    /**
     * @param r alert rule.
     */
    public AlertRuleBean(final AlertRule r) {
        super();
        setType(r.getType());
        setId(r.getId());
    }
    /**
     * @return the type
     */
    public AlertType getType() {
        return type;
    }
    /**
     * @param type the type to set
     */
    public void setType(final AlertType type) {
        this.type = type;
    }
    /**
     * @return the id
     */
    public Long getId() {
        return id;
    }
    /**
     * @param id the id to set
     */
    public void setId(final Long id) {
        this.id = id;
    }
}
