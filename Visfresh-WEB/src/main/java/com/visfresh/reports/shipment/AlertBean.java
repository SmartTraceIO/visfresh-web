/**
 *
 */
package com.visfresh.reports.shipment;

import com.visfresh.entities.AlertType;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class AlertBean {
    private AlertType type;
    private String text;

    /**
     * Default constructor.
     */
    public AlertBean() {
        super();
    }
    /**
     * @param type alert type.
     * @param text alert text.
     */
    public AlertBean(final AlertType type, final String text) {
        super();
        this.type = type;
        this.text = text;
    }


    public AlertType getType() {
        return type;
    }
    public void setType(final AlertType type) {
        this.type = type;
    }
    public String getText() {
        return text;
    }
    public void setText(final String text) {
        this.text = text;
    }
}
