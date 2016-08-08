/**
 *
 */
package com.visfresh.utils;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.visfresh.dao.DaoBase;
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
    /**
     * @param locationProfileDao
     * @param ids
     * @return
     */
    public static
        <E extends EntityWithId<ID>,ID extends Serializable & Comparable<ID>>
            Map<ID, E> resolveEntities(final DaoBase<E, ID> dao, final Set<ID> ids) {
        final Map<ID, E> map = new HashMap<>();
        for (final E e : dao.findAll(ids)) {
            map.put(e.getId(), e);
        }
        return map;
    }
    /**
     * @param entities collection of entities.
     * @param id entity ID.
     * @return entity from collection for given ID.
     */
    public static <E extends EntityWithId<ID>,ID extends Serializable & Comparable<ID>>
            E getEntity(final Collection<E> entities, final ID id) {
        for (final E e : entities) {
            if (e.getId().equals(id)) {
                return e;
            }
        }
        return null;
    }
}
