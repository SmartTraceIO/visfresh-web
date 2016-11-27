/**
 *
 */
package com.visfresh.utils;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.visfresh.dao.DaoBase;
import com.visfresh.entities.EntityWithId;
import com.visfresh.entities.Shipment;
import com.visfresh.entities.TemperatureAlert;
import com.visfresh.entities.TemperatureRule;

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
    public static <E extends EntityWithId<ID>,ID extends Serializable & Comparable<ID>>
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
    public static <ID extends Serializable & Comparable<ID>> CompareResult<ID>
        compare(final Collection<ID> origin, final Collection<ID> modified) {

        final Set<ID> originSet = new HashSet<>(origin);
        final Set<ID> modifiedSet = new HashSet<>(modified);

        //remove items existing in both collections
        final Iterator<ID> iter = originSet.iterator();
        while (iter.hasNext()) {
            final ID next = iter.next();
            if (modifiedSet.remove(next)) {
                iter.remove();
            }
        }

        //as result first set contains only deleted entries
        //second set contains only added

        final CompareResult<ID> result = new CompareResult<>();
        result.getAdded().addAll(modifiedSet);
        result.getDeleted().addAll(originSet);

        //sort result
        Collections.sort(result.getAdded());
        Collections.sort(result.getDeleted());

        return result;
    }
    /**
     * @param shipment
     * @param listShipmentAlerts
     * @return
     */
    public static Set<TemperatureRule> getTemperatureRules(final List<TemperatureAlert> listShipmentAlerts) {
        final Set<TemperatureRule> rules = new HashSet<>();
        for (final TemperatureAlert a : listShipmentAlerts) {
            final Shipment shipment = a.getShipment();
            TemperatureRule rule = null;

            for (final TemperatureRule r : shipment.getAlertProfile().getAlertRules()) {
                if (r.getId().equals(a.getRuleId())) {
                    rule = r;
                    break;
                }
            }

            if (rule != null) { //old version where rule ID has not set
                rules.add(rule);
            }
        }
        return rules;
    }
}
