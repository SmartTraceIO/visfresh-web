/**
 *
 */
package com.visfresh.io.shipment;

import com.visfresh.entities.Alert;
import com.visfresh.entities.AlertType;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class AlertBean extends NotificationIssueBean {
    /**
     * Alert type.
     */
    private AlertType type;

    /**
     * Default constructor.
     */
    public AlertBean() {
        super();
    }
    /**
     * Default constructor.
     */
    public AlertBean(final Alert a) {
        super(a);
        setType(a.getType());
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
}
