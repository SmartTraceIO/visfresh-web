/**
 *
 */
package com.visfresh.controllers.audit;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.method.HandlerMethod;

import com.visfresh.entities.RestSession;
import com.visfresh.services.RestSessionManager;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class CurrentSessionHolder implements Auditor {
    @Autowired
    private AuditInterceptor ai;
    @Autowired
    private RestSessionManager m;

    private static final ThreadLocal<RestSession> currentSession = new ThreadLocal<>();

    /**
     * Default constructor.
     */
    public CurrentSessionHolder() {
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
     * @see com.visfresh.controllers.audit.Auditor#preInvoke(org.springframework.web.context.request.ServletWebRequest, org.springframework.web.method.HandlerMethod)
     */
    @Override
    public void preInvoke(final ServletWebRequest req, final HandlerMethod method) {
        if (isRestMethod(method)) {
            final String pathInfo = req.getRequest().getPathInfo();
            final String token = getToken(pathInfo);

            currentSession.set(m.getSession(token));
        }
    }
    /* (non-Javadoc)
     * @see com.visfresh.controllers.audit.Auditor#postInvoke(org.springframework.web.context.request.ServletWebRequest, org.springframework.web.method.HandlerMethod, java.lang.Exception)
     */
    @Override
    public void postInvoke(final ServletWebRequest req, final HandlerMethod method, final Exception e) {
        currentSession.set(null);
    }
    /**
     * @return current thread local REST session.
     */
    public static RestSession getCurrentSession() {
        return currentSession.get();
    }

    /**
     * @param method
     * @return
     */
    private boolean isRestMethod(final HandlerMethod method) {
        return method.getMethodAnnotation(RequestMapping.class) != null;
    }
    /**
     * @param pathInfo
     * @return
     */
    private String getToken(final String pathInfo) {
        return pathInfo.substring(pathInfo.lastIndexOf('/') + 1);
    }
}
