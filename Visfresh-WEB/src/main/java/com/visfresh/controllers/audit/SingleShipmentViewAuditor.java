/**
 *
 */
package com.visfresh.controllers.audit;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.method.HandlerMethod;

import com.visfresh.controllers.ShipmentController;
import com.visfresh.controllers.session.SessionManagerListener;
import com.visfresh.dao.ShipmentDao;
import com.visfresh.entities.RestSession;
import com.visfresh.entities.Role;
import com.visfresh.entities.Shipment;
import com.visfresh.entities.User;
import com.visfresh.services.RestSessionManager;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class SingleShipmentViewAuditor implements Auditor, SessionManagerListener {
    /**
     *
     */
    private static final String VIEWED_SINGLE_SHIPMENT = "ViewedSingleShipment";

    private static final Logger log = LoggerFactory.getLogger(SingleShipmentViewAuditor.class);

    @Autowired
    private AuditInterceptor ai;
    @Autowired
    private RestSessionManager m;
    @Autowired
    private ShipmentDao dao;

    /**
     * Default constructor.
     */
    public SingleShipmentViewAuditor() {
        super();
    }

    @PostConstruct
    public void init() {
        ai.addAuditor(this);
    }
    @PreDestroy
    public void destroy() {
        ai.removeAuditor(this);
    }

    /* (non-Javadoc)
     * @see com.visfresh.controllers.audit.Auditor#postInvoke(org.springframework.web.context.request.ServletWebRequest, org.springframework.web.method.HandlerMethod, java.lang.Exception)
     */
    @Override
    public void postInvoke(final ServletWebRequest req, final HandlerMethod method, final Exception e) {
        if (e == null && isGetSingleShipment(method)) {
            final String pathInfo = req.getRequest().getPathInfo();
            final String token = getToken(pathInfo);

            final RestSession s = m.getSession(token);
            if (s != null) {
                final Shipment shipment = findShipment(req.getRequest());
                if (shipment != null && hasAccess(s.getUser(), shipment)) {
                    handleShipmentViewed(s, shipment);
                }
            }
        }
    }

    /**
     * @param s REST session.
     * @param shipment shipment.
     */
    private void handleShipmentViewed(final RestSession s, final Shipment shipment) {
        final String p = s.getProperty(VIEWED_SINGLE_SHIPMENT);
        final Long old = p == null ? null : Long.valueOf(p);
        final User user = s.getUser();

        if (shipment.getId().equals(p)) {
            log.debug("User " + user.getEmail() + " continuing to view the shipment "
                    + shipment.getId());
        } else {
            if (old != null) {
                log.debug("User " + user.getEmail() + " has stopped to view the shipment "
                        + old);
                handleStoppedToView(user, old);
            }

            log.debug("User " + user.getEmail() + " started to view the shipment "
                    + shipment.getId());
            handleStartedToView(user, shipment.getId());

            //update session value
            s.putProperty(VIEWED_SINGLE_SHIPMENT, shipment.getId().toString());
        }
    }

    /**
     * @param user
     * @param old
     */
    private void handleStoppedToView(final User user, final Long old) {
        // TODO Auto-generated method stub

    }
    /**
     * @param user
     * @param id
     */
    private void handleStartedToView(final User user, final Long id) {
        // TODO Auto-generated method stub

    }

    /**
     * @param user user.
     * @param shipment shipment.
     * @return true if the user has access to given shipment.
     */
    private boolean hasAccess(final User user, final Shipment shipment) {
        return Role.BasicUser.hasRole(user)
                && ShipmentController.hasViewSingleShipmentAccess(user, shipment);
    }

    /**
     * @param parameters request parameters.
     * @return shipment.
     */
    private Shipment findShipment(final HttpServletRequest req) {
        //attempt to find the shipment by ID.
        final String shipmentId = req.getParameter("shipmentId");
        if (shipmentId != null) {
            return dao.findOne(Long.parseLong(shipmentId));
        }

        //find shipment by serial number and trip count
        final String sn = req.getParameter("sn");
        final String tripCount = req.getParameter("trip");
        if (sn != null && tripCount != null) {
            return dao.findBySnTrip(sn, Integer.parseInt(tripCount));
        }

        return null;
    }

    /**
     * @param pathInfo
     * @return
     */
    private String getToken(final String pathInfo) {
        final int offset = pathInfo.indexOf(ShipmentController.GET_SINGLE_SHIPMENT);
        return pathInfo.substring(offset + ShipmentController.GET_SINGLE_SHIPMENT.length() + 1);
    }

    /* (non-Javadoc)
     * @see com.visfresh.controllers.audit.Auditor#preInvoke(org.springframework.web.context.request.ServletWebRequest, org.springframework.web.method.HandlerMethod)
     */
    @Override
    public void preInvoke(final ServletWebRequest req, final HandlerMethod method) {
    }

    /**
     * @param method method.
     * @return true if the getSingleShipment has call
     */
    private boolean isGetSingleShipment(final HandlerMethod method) {
        return "getSingleShipment".equals(method.getMethod().getName());
    }
    /* (non-Javadoc)
     * @see com.visfresh.controllers.session.SessionManagerListener#sessionClosed(com.visfresh.entities.RestSession)
     */
    @Override
    public void sessionClosed(final RestSession session) {
        //if session closed, then finished viewing of shipment if viewed
        final String shipment = session.getProperty(VIEWED_SINGLE_SHIPMENT);
        if (shipment != null) {
            handleStoppedToView(session.getUser(), Long.valueOf(shipment));
        }
    }
    /* (non-Javadoc)
     * @see com.visfresh.controllers.session.SessionManagerListener#sessionCreated(com.visfresh.entities.RestSession)
     */
    @Override
    public void sessionCreated(final RestSession session) {}
}
