/**
 *
 */
package com.visfresh.dao.impl;

import java.io.Serializable;
import java.util.Map;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class EntityCache <ID extends Serializable&Comparable<ID>> extends DefaultCache<Map<String, Object>, ID> {
    /**
     * Default constructor
     */
    public EntityCache(final String name, final int maxEntriesInCache, final int timeToIdleSeconds, final int timeToLiveSeconds) {
        super(name, maxEntriesInCache, timeToIdleSeconds, timeToLiveSeconds);
    }
}
