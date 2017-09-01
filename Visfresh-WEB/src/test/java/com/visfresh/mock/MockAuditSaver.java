/**
 *
 */
package com.visfresh.mock;

import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.visfresh.entities.ShipmentAuditItem;
import com.visfresh.impl.services.AuditSaver;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class MockAuditSaver extends AuditSaver {
    /**
     * Logger.
     */
    private static final Logger log = LoggerFactory.getLogger(MockAuditSaver.class);
    private List<ShipmentAuditItem> items = new LinkedList<>();

    /**
     * Default constructor.
     */
    public MockAuditSaver() {
        super();
    }

    /* (non-Javadoc)
     * @see com.visfresh.impl.services.AuditSaver#auditItemCreated(com.visfresh.entities.ShipmentAuditItem)
     */
    @Override
    public void auditItemCreated(final ShipmentAuditItem item) {
        items.add(item);
        log.debug("Audit item (" + item.getShipmentId() + ", action: " + item.getAction() + ") saved");
    }
    /**
     * @return the items
     */
    public List<ShipmentAuditItem> getItems() {
        return items;
    }
    /**
     * Cleares the items.
     */
    public void clear() {
        items.clear();
    }
}
