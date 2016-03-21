/**
 *
 */
package com.visfresh.mock;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.visfresh.entities.Shipment;
import com.visfresh.services.ShipmentShutdownService;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class MockShipmentShutdownService implements ShipmentShutdownService {
    private final Map<Long, Date> shutdowns = new HashMap<>();

    /**
     * Default constructor.
     */
    public MockShipmentShutdownService() {
        super();
    }
    /* (non-Javadoc)
     * @see com.visfresh.services.ShipmentShutdownService#sendShipmentShutdown(com.visfresh.entities.Shipment, java.util.Date)
     */
    @Override
    public void sendShipmentShutdown(final Shipment shipment, final Date date) {
        shutdowns.put(shipment.getId(), date);
    }
    /**
     * @param id shipment ID.
     * @return shutdown date.
     */
    public Date getShutdownDate(final Long id) {
        return shutdowns.get(id);
    }
}
