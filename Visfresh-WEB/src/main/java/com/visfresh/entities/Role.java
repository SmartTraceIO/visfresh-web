/**
 *
 */
package com.visfresh.entities;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public enum Role {
//    GlobalAdmin(),
//    CompanyAdmin(),
//    Dispatcher(),
//    ShipmentViewer(),
//    Setup();
    /**
     * Add company, add initial users, add devices,
     * This user not visible to other users
     */
    SmartTraceAdmin(SpringRoles.SmartTraceAdmin),
    /**
     * Company admin
     */
    Admin(SpringRoles.Admin),
    /**
     * View Shipments,
     * Shipment Detail (inc. Suppress Alerts/Shutdown device/AddNotes)
     * NewShipment,
     * (inc. Shutdown Device, Deactivate Device, Billing)
     * Setup pages (inc. Users)
     * Trackers,
     * NewShipment,
     * View Shipments,
     * Shipment Detail (inc.Suppress Alerts/Shutdown device/AddNotes),
     * Trackers (inc. Shutdown Device, but NOT Deactivate Device or Billing)
     * Setup pages (but Users is View Only),
     */
    BasicUser(SpringRoles.BasicUser),
    /**
     * NOT NewShipment
     * View Shipments,
     * Shipment Detail (NOT Suppress Alerts/Shutdown device/AddNotes),
     * Trackers (NOT Shutdown Device, Deactivate Device or Billing)
     * NOT Setup pages
     */
    NormalUser(SpringRoles.NormalUser);

    static {
        NormalUser.includedRoles = new Role[]{BasicUser};
        checkCorrectSpringRoles();
    }

    private Role[] includedRoles = {};
    private final String springRolePart;

    /**
     *
     */
    private Role(final String springRole) {
        if (!springRole.startsWith(SpringRoles.SPRING_ROLE_PREFIX)) {
            throw new RuntimeException("Incorrect spring role name " + springRole);
        }
        springRolePart = springRole.substring(SpringRoles.SPRING_ROLE_PREFIX.length());
    }
    /**
     *
     */
    private static void checkCorrectSpringRoles() {
        for (final Role r : values()) {
            if (!r.name().equals(r.springRolePart)) {
                throw new RuntimeException("Incorrect spring role name " + SpringRoles.SPRING_ROLE_PREFIX + r.springRolePart);
            }
        }

    }
    /**
     * @param r role.
     * @return true if has given role.
     */
    private boolean hasRole(final Role r) {
        if (this == r || this == SmartTraceAdmin || this == Admin && r != SmartTraceAdmin) {
            return true;
        }

        for (final Role role : includedRoles) {
            if (role.hasRole(r)) {
                return true;
            }
        }

        return false;
    }
    /**
     * @param user the user.
     * @return true if given user has given role.
     */
    public boolean hasRole(final User user) {
        if (user != null && user.getRoles() != null) {
            for (final Role r : user.getRoles()) {
                if (r.hasRole(this)) {
                    return true;
                }
            }
        }
        return false;
    }
}
