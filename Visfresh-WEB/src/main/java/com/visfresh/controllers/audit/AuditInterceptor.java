/**
 *
 */
package com.visfresh.controllers.audit;

import java.util.LinkedList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class AuditInterceptor extends HandlerInterceptorAdapter {
    /**
     * The logger.
     */
    private static final Logger log = LoggerFactory.getLogger(AuditInterceptor.class);
    /**
     * The listener list.
     */
    private final List<Auditor> listeners = new LinkedList<>();

    /**
     * Default constructor.
     */
    public AuditInterceptor() {
        super();
    }

    /* (non-Javadoc)
     * @see org.springframework.web.servlet.HandlerInterceptor#preHandle(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, java.lang.Object)
     */
    @Override
    public boolean preHandle(final HttpServletRequest request, final HttpServletResponse response, final Object handler)
            throws Exception {
        if (handler instanceof HandlerMethod) {
            final ServletWebRequest req = new ServletWebRequest(request, response);
            final HandlerMethod method = (HandlerMethod) handler;

            for (final Auditor auditor : getAuditors()) {
                try {
                    auditor.preInvoke(req, method);
                } catch (final Exception e) {
                    log.error("Failed to call auditor", e);
                }
            }
        }

        return true;
    }

    /* (non-Javadoc)
     * @see org.springframework.web.servlet.HandlerInterceptor#postHandle(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, java.lang.Object, org.springframework.web.servlet.ModelAndView)
     */
    @Override
    public void postHandle(final HttpServletRequest request, final HttpServletResponse response, final Object handler,
            final ModelAndView modelAndView) throws Exception {
    }

    /* (non-Javadoc)
     * @see org.springframework.web.servlet.HandlerInterceptor#afterCompletion(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, java.lang.Object, java.lang.Exception)
     */
    @Override
    public void afterCompletion(final HttpServletRequest request, final HttpServletResponse response, final Object handler, final Exception ex)
            throws Exception {
        final ServletWebRequest req = new ServletWebRequest(request, response);
        final HandlerMethod method = (HandlerMethod) handler;

        for (final Auditor auditor : getAuditors()) {
            try {
                auditor.postInvoke(req, method, ex);
            } catch (final Exception exc) {
                log.error("Failed to call auditor", exc);
            }
        }
    }
    /**
     * @param a auditor to add.
     */
    public void addAuditor(final Auditor a) {
        synchronized (listeners) {
            listeners.add(a);
        }
    }
    /**
     * @param a auditor to remove.
     */
    public void removeAuditor(final Auditor a) {
        synchronized (listeners) {
            listeners.remove(a);
        }
    }
    /**
     * @return list of listeners.
     */
    protected List<Auditor> getAuditors() {
        synchronized (listeners) {
            return new LinkedList<>(listeners);
        }
    }
}
