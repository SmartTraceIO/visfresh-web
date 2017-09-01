/**
 *
 */
package com.visfresh.impl.services;

import javax.annotation.PreDestroy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.visfresh.dao.ShipmentAuditDao;
import com.visfresh.entities.ShipmentAuditItem;
import com.visfresh.services.ShipmentAuditListener;
import com.visfresh.services.ShipmentAuditService;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class AuditSaver implements ShipmentAuditListener {
    private static final Logger log = LoggerFactory.getLogger(AuditSaver.class);

    @Autowired
    private ShipmentAuditDao dao;

    private ShipmentAuditService service;

    private AsyncEventHelper<ShipmentAuditItem> helper = new AsyncEventHelper<ShipmentAuditItem>() {
        /* (non-Javadoc)
         * @see com.visfresh.impl.services.AsyncEventHelper#processEvent(java.lang.Object)
         */
        @Override
        protected void processEvent(final ShipmentAuditItem e) {
            dao.save(e);
            log.debug("Audit item saved (" + e.getShipmentId() + ", action: " + e.getAction() + ")");
        }
    };

    /**
     * Default constructor.
     */
    public AuditSaver() {
        super();
    }

    @Autowired
    private void setAuditService(final ShipmentAuditService s) {
        service = s;
        service.addAuditListener(this);
    }
    @PreDestroy
    private void destroy() {
        service.removeAuditListener(this);
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
}
