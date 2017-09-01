/**
 *
 */
package com.visfresh.impl.services;

import javax.annotation.PreDestroy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.visfresh.entities.ShipmentAuditItem;
import com.visfresh.services.ShipmentAuditListener;
import com.visfresh.services.ShipmentAuditService;
import com.visfresh.services.SingleShipmentService;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class SingleShipmentDataUpdater implements ShipmentAuditListener {
    private static final Logger log = LoggerFactory.getLogger(SingleShipmentDataUpdater.class);

    @Autowired
    private SingleShipmentService service;
    private ShipmentAuditService audit;

    private AsyncEventHelper<ShipmentAuditItem> helper = new AsyncEventHelper<ShipmentAuditItem>() {
        /* (non-Javadoc)
         * @see com.visfresh.impl.services.AsyncEventHelper#processEvent(java.lang.Object)
         */
        @Override
        protected void processEvent(final ShipmentAuditItem e) {
            updateSingleShipmentDate(e.getShipmentId());
        }
    };

    /**
     * Default constructor.
     */
    public SingleShipmentDataUpdater() {
        super();
    }

    @Autowired
    private void setAuditService(final ShipmentAuditService s) {
        audit = s;
        audit.addAuditListener(this);
    }
    @PreDestroy
    private void destroy() {
        audit.removeAuditListener(this);
        helper.destroy();
        log.debug("Audit service saver has destroyed");
    }

    /* (non-Javadoc)
     * @see com.visfresh.services.ShipmentAuditListener#auditItemCreated(com.visfresh.entities.ShipmentAuditItem)
     */
    @Override
    public void auditItemCreated(final ShipmentAuditItem item) {
        helper.addToHandle(item);
    }
    /**
     * @param shipmentId
     */
    protected void updateSingleShipmentDate(final long shipmentId) {
        service.rebuildShipmentData(shipmentId);
    }
}
