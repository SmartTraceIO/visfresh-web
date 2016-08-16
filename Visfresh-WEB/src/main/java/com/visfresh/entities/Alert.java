/**
 *
 */
package com.visfresh.entities;


/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class Alert extends NotificationIssue {
    /**
     * Alert type.
     */
    private AlertType type;
    /**
     * Default constructor.
     */
    public Alert() {
        super();
    }
    /**
     * @param type alert type.
     */
    public Alert(final AlertType type) {
        super();
        this.type = type;
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
