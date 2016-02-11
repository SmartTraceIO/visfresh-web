/**
 *
 */
package com.visfresh.entities;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class AlertRule {
    private AlertType type;
    private Long id;

    /**
     * Default constructor.
     */
    public AlertRule(final AlertType type) {
        super();
        setType(type);
    }
    /**
     * Default constructor.
     */
    public AlertRule() {
        super();
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
     * @return
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
