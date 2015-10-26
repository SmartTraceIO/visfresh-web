/**
 *
 */
package com.visfresh.entities;

import java.io.Serializable;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public interface EntityWithId<ID extends Serializable & Comparable<ID>> {
    ID getId();
}
