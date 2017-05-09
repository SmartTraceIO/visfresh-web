/**
 *
 */
package com.visfresh.mock;

import java.util.LinkedList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.visfresh.entities.ShipmentAuditItem;
import com.visfresh.mpl.services.DefaultShipmentAuditService;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class MockShipmentAuditService extends DefaultShipmentAuditService {
    private List<ShipmentAuditItem> items = new LinkedList<>();

    /**
     * Default constructor.
     */
    public MockShipmentAuditService() {
        super();
    }

    /* (non-Javadoc)
     * @see com.visfresh.services.DefaultShipmentAuditService#save(com.visfresh.entities.ShipmentAuditItem)
     */
    @Override
    protected void save(final ShipmentAuditItem item) {
        items.add(item);
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
