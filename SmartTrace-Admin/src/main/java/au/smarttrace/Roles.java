/**
 *
 */
package au.smarttrace;

/**
 * The role names have not java upper case style for names be equals by roles of WEB project.
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public final class Roles {
    /**
     * Private constructor.
     */
    private Roles() {
        super();
    }

    /**
     * Add company, add initial users, add devices,
     * This user not visible to other users
     */
    public static final String SmartTraceAdmin = "SmartTraceAdmin";
    /**
     * Company admin
     */
    public static final String Admin = "Admin";
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
    public static final String BasicUser = "BasicUser";
    /**
     * NOT NewShipment
     * View Shipments,
     * Shipment Detail (NOT Suppress Alerts/Shutdown device/AddNotes),
     * Trackers (NOT Shutdown Device, Deactivate Device or Billing)
     * NOT Setup pages
     */
    public static final String NormalUser = "NormalUser";
}
