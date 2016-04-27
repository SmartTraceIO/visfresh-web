/**
 *
 */
package com.visfresh.utils;

import java.io.Serializable;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import com.visfresh.entities.EntityWithId;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public final class EntityUtils {
    /**
     * Default constructor.
     */
    private EntityUtils() {
        super();
    }
    /**
     * @param e entity.
     * @return ID.
     */
    public static <ID extends Serializable & Comparable<ID>> ID getEntityId(final EntityWithId<ID> e) {
        return e == null ? null : e.getId();
    }
    /**
     * @param entities
     * @return
     */
    public static <ID extends Serializable & Comparable<ID>> Collection<ID> getIdList(
            final List<? extends EntityWithId<ID>> entities) {
        final List<ID> list = new LinkedList<ID>();
        for (final EntityWithId<ID> e : entities) {
            list.add(getEntityId(e));
        }
        return list;
    }
}
