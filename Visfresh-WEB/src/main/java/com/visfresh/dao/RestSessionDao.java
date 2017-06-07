/**
 *
 */
package com.visfresh.dao;

import com.visfresh.entities.RestSession;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public interface RestSessionDao extends DaoBase<RestSession, RestSession, Long> {
    /**
     * @param token token.
     * @return REST session.
     */
    RestSession findByToken(String token);
}
