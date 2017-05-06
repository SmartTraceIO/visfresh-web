/**
 *
 */
package com.visfresh.controllers.audit;

import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.method.HandlerMethod;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public interface Auditor {
    public void preInvoke(ServletWebRequest req, HandlerMethod method);
    public void postInvoke(ServletWebRequest req, HandlerMethod method, Exception e);
}
