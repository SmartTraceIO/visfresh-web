/**
 *
 */
package com.visfresh.io;

import com.visfresh.entities.User;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public interface UserResolver {
    User getUser(Long id);
}
