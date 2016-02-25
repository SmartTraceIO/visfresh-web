/**
 *
 */
package com.visfresh.entities;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public enum Role {
    GlobalAdmin(100),
    CompanyAdmin(70),
    Dispatcher(40),
    ShipmentViewer(25),
    Setup(24);

    private int priority;
    Role(final int priority) {
        this.priority = priority;
    }
    /**
     * @return
     */
    public boolean havePermissions(final Role r) {
        return r == null || priority >= r.priority;
    }
}
