/**
 *
 */
package com.visfresh.controllers.session;

import com.visfresh.entities.RestSession;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public interface SessionManagerListener {
    void sessionCreated(RestSession session);
    void sessionClosed(RestSession session);
}
