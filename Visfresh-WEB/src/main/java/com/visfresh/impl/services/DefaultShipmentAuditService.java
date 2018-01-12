/**
 *
 */
package com.visfresh.impl.services;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.visfresh.controllers.AbstractController;
import com.visfresh.controllers.audit.ShipmentAuditAction;
import com.visfresh.entities.RestSession;
import com.visfresh.entities.ShipmentAuditItem;
import com.visfresh.entities.User;
import com.visfresh.services.ShipmentAuditListener;
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
    /**
     * Listener list.
     */
    private final List<ShipmentAuditListener> listeners = new CopyOnWriteArrayList<>();

    /**
     * Default constructor.
     */
    public DefaultShipmentAuditService() {
        super();
    }

    /* (non-Javadoc)
     * @see com.visfresh.services.ShipmentAuditService#addAuditListener(com.visfresh.services.ShipmentAuditListener)
     */
    @Override
    public void addAuditListener(final ShipmentAuditListener l) {
        listeners.add(l);
    }
    /* (non-Javadoc)
     * @see com.visfresh.services.ShipmentAuditService#removeAuditListener(com.visfresh.services.ShipmentAuditListener)
     */
    @Override
    public void removeAuditListener(final ShipmentAuditListener l) {
        listeners.remove(l);
    }

    /* (non-Javadoc)
     * @see com.visfresh.services.ShipmentAuditService#handleShipmentAction(com.visfresh.entities.Shipment, com.visfresh.entities.User, com.visfresh.controllers.audit.ShipmentAuditAction, java.util.Map)
     */
    @Override
    public void handleShipmentAction(final Long shipmentId, final User user,
            final ShipmentAuditAction action, final Map<String, String> details) {
        boolean shouldSaveAudit = true;

        if (user != null) {
            final RestSession session = AbstractController.getSession();

            if (session == null) {
                log.error("Current session is not bound to ThreadLocal for user "
                        + user.getEmail(), new IllegalStateException("Current session not bound"));
                if (!isAuto(action)) {
                    shouldSaveAudit = false;
                }
            } else {
                final String oldValue = session.getProperty(SHIPMENT_AUDIT);
                final String value = buildSessionAuditValue(action, shipmentId);

                final boolean isChanged = !value.equals(oldValue);

                if (isViewedSingleShipment(action) && !isChanged) {
                    //only if shipment viewing has changed should save audit.
                    shouldSaveAudit = false;
                }

                if (isChanged) {
                    session.putProperty(SHIPMENT_AUDIT, value);
                }
            }
        } else if (!isAuto(action)) {
            shouldSaveAudit = false;
        }

        if (shouldSaveAudit) {
            final ShipmentAuditItem item = createAuditItem(shipmentId, user, action, details);
            notifyListeners(item);
        }
    }

    /**
     * @param action
     * @return
     */
    protected boolean isAuto(final ShipmentAuditAction action) {
        return action == ShipmentAuditAction.Autocreated;
    }

    /**
     * @param action audit action.
     * @param shipment shipment.
     * @return session value for given action and shipment.
     */
    private String buildSessionAuditValue(final ShipmentAuditAction action, final Long shipmentId) {
        return action + "-" + shipmentId;
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
    protected void notifyListeners(final ShipmentAuditItem item) {
        for (final ShipmentAuditListener l : listeners) {
            try {
                l.auditItemCreated(item);
            } catch (final Throwable e) {
                log.error("Faile to handle audit item with listener " + l.getClass().getName(), e);
            }
        }
    }
    /**
     * @param shipment the shipment.
     * @param user the user.
     * @param action the audit action.
     * @param details audit details.
     * @return shipment audit item.
     */
    protected ShipmentAuditItem createAuditItem(final Long shipmentId, final User user,
            final ShipmentAuditAction action, final Map<String, String> details) {
        final ShipmentAuditItem item = new ShipmentAuditItem();
        item.setShipmentId(shipmentId);
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
