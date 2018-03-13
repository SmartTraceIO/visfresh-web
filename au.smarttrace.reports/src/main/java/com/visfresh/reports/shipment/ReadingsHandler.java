/**
 *
 */
package com.visfresh.reports.shipment;

import com.visfresh.entities.AlertType;
import com.visfresh.entities.ShortTrackerEvent;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public interface ReadingsHandler {

    /**
     * @param e event.
     * @param alerts alerts.
     */
    void handleEvent(ShortTrackerEvent e, AlertType[] alerts);
    Long getShipmentId(String sn, int tripCount);
}
