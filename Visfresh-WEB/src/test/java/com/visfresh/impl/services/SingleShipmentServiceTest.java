/**
 *
 */
package com.visfresh.impl.services;

import java.util.Date;
import java.util.List;

import com.visfresh.entities.Alert;
import com.visfresh.entities.AlertRule;
import com.visfresh.entities.InterimStop;
import com.visfresh.entities.Shipment;
import com.visfresh.io.shipment.SingleShipmentBean;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class SingleShipmentServiceTest extends SingleShipmentServiceImpl {

    /**
     * Default constructor.
     */
    public SingleShipmentServiceTest() {
        super();
    }

    /* (non-Javadoc)
     * @see com.visfresh.impl.services.SingleShipmentServiceImpl#getAlerts(com.visfresh.entities.Shipment)
     */
    @Override
    protected List<Alert> getAlerts(final Shipment s) {
        // TODO Auto-generated method stub
        return super.getAlerts(s);
    }
    /* (non-Javadoc)
     * @see com.visfresh.impl.services.SingleShipmentServiceImpl#getAlertsSuppressionDate(com.visfresh.entities.Shipment)
     */
    @Override
    protected Date getAlertsSuppressionDate(final Shipment s) {
        // TODO Auto-generated method stub
        return super.getAlertsSuppressionDate(s);
    }
    /* (non-Javadoc)
     * @see com.visfresh.impl.services.SingleShipmentServiceImpl#getAlertYetFoFireImpl(com.visfresh.entities.Shipment)
     */
    @Override
    protected List<AlertRule> getAlertYetFoFireImpl(final Shipment s) {
        // TODO Auto-generated method stub
        return super.getAlertYetFoFireImpl(s);
    }
    /* (non-Javadoc)
     * @see com.visfresh.impl.services.SingleShipmentServiceImpl#getBeanIncludeSiblings(long)
     */
    @Override
    protected List<SingleShipmentBean> getBeanIncludeSiblings(final long shipmentId) {
        // TODO Auto-generated method stub
        return super.getBeanIncludeSiblings(shipmentId);
    }
    /* (non-Javadoc)
     * @see com.visfresh.impl.services.SingleShipmentServiceImpl#getBeanIncludeSiblings(java.lang.String, int)
     */
    @Override
    protected List<SingleShipmentBean> getBeanIncludeSiblings(final String sn, final int tripCount) {
        // TODO Auto-generated method stub
        return super.getBeanIncludeSiblings(sn, tripCount);
    }
    /* (non-Javadoc)
     * @see com.visfresh.impl.services.SingleShipmentServiceImpl#getInterimStops(com.visfresh.entities.Shipment)
     */
    @Override
    protected List<InterimStop> getInterimStops(final Shipment s) {
        // TODO Auto-generated method stub
        return super.getInterimStops(s);
    }
}
