/**
 *
 */
package com.visfresh.mpl.services;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.visfresh.controllers.audit.CurrentSessionHolder;
import com.visfresh.controllers.audit.ShipmentAuditAction;
import com.visfresh.dao.ShipmentAuditDao;
import com.visfresh.entities.RestSession;
import com.visfresh.entities.Shipment;
import com.visfresh.entities.ShipmentAuditItem;
import com.visfresh.entities.User;
import com.visfresh.services.ShipmentAuditService;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class DefaultShipmentAuditService implements ShipmentAuditService {
    private static final Logger log = LoggerFactory.getLogger(DefaultShipmentAuditService.class);

    /**
     * Shipment audit session property key.
     */
    private static final String SHIPMENT_AUDIT = "ShipmentAudit";
    @Autowired
    private ShipmentAuditDao dao;

    /**
     * Default constructor.
     */
    public DefaultShipmentAuditService() {
        super();
    }

    /* (non-Javadoc)
     * @see com.visfresh.services.ShipmentAuditService#handleShipmentAction(com.visfresh.entities.Shipment, com.visfresh.entities.User, com.visfresh.controllers.audit.ShipmentAuditAction, java.util.Map)
     */
    @Override
    public void handleShipmentAction(final Shipment shipment, final User user,
            final ShipmentAuditAction action, final Map<String, String> details) {
        boolean shouldSaveAudit = true;

        if (user != null) {
            final RestSession session = CurrentSessionHolder.getCurrentSession();

            if (session == null) {
                log.error("Current session is not bound to ThreadLocal for user "
                        + user.getEmail(), new IllegalStateException("Current session not bound"));
            } else {
                final String oldValue = session.getProperty(SHIPMENT_AUDIT);
                final String value = buildSessionAuditValue(action, shipment);

                final boolean isChanged = !value.equals(oldValue);

                if (isViewedSingleShipment(action) && !isChanged) {
                    //only if shipment viewing has changed should save audit.
                    shouldSaveAudit = false;
                }

                if (isChanged) {
                    session.putProperty(SHIPMENT_AUDIT, value);
                }
            }
        }

        if (shouldSaveAudit) {
            final ShipmentAuditItem item = createAuditItem(shipment, user, action, details);
            save(item);
        }
    }

    /**
     * @param action audit action.
     * @param shipment shipment.
     * @return session value for given action and shipment.
     */
    private String buildSessionAuditValue(final ShipmentAuditAction action, final Shipment shipment) {
        return action + "-" + shipment.getId();
    }

    /**
     * @param action shipment audit action.
     * @return true if one from getSingleShipment versions.
     */
    private boolean isViewedSingleShipment(final ShipmentAuditAction action) {
        return action == ShipmentAuditAction.Viewed || action == ShipmentAuditAction.ViewedLite;
    }

    /**
     * @param item
     */
    protected void save(final ShipmentAuditItem item) {
        dao.save(item);
    }

    /**
     * @param shipment the shipment.
     * @param user the user.
     * @param action the audit action.
     * @param details audit details.
     * @return shipment audit item.
     */
    protected ShipmentAuditItem createAuditItem(final Shipment shipment, final User user,
            final ShipmentAuditAction action, final Map<String, String> details) {
        final ShipmentAuditItem item = new ShipmentAuditItem();
        item.setShipmentId(shipment.getId());
        if (user != null) {
            item.setUserId(user.getId());
        }
        item.setAction(action);
        if (details != null) {
            item.getAdditionalInfo().putAll(details);
        }
        return item;
    }
}
