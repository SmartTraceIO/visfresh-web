/**
 *
 */
package com.visfresh.controllers.restclient;

import java.net.URLConnection;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public interface ConnectionInterceptor {
    void beforeSend(URLConnection con);
    void beforeReceive(URLConnection con);
}
