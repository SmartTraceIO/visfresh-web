/**
 *
 */
package com.visfresh.dao.impl;

import java.io.Serializable;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import net.sf.ehcache.config.CacheConfiguration;
import net.sf.ehcache.config.CacheConfiguration.TransactionalMode;
import net.sf.ehcache.store.MemoryStoreEvictionPolicy;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class DefaultCache <T, Key extends Serializable> {
    private Cache cache;

    /**
     * Default constructor
     */
    public DefaultCache(final String name, final int maxEntriesInCache, final int timeToIdleSeconds, final int timeToLiveSeconds) {
        super();
        final CacheConfiguration cfg = new CacheConfiguration();
        //cache name
        cfg.setName(name);
        cfg.setEternal(false);
        cfg.transactionalMode(TransactionalMode.OFF);
        cfg.setMemoryStoreEvictionPolicyFromObject(MemoryStoreEvictionPolicy.LRU);
        cfg.setOverflowToOffHeap(false);
        cfg.setTimeToIdleSeconds(timeToIdleSeconds);
        cfg.setTimeToLiveSeconds(timeToLiveSeconds);
        cfg.setMaxEntriesLocalHeap(maxEntriesInCache);
        cache = new Cache(cfg);
    }

    /**
     * Initializes the cache.
     */
    public void initialize() {
        CacheManager.getInstance().addCache(cache);
    }
    /**
     * Shuts down the cache.
     */
    public void destroy() {
        cache.dispose();
    }
    /**
     * @param id entity ID.
     * @param value entity value.
     */
    public void put(final Key id, final T value) {
        final Element e = new Element(id, value);
        cache.put(e);
    }
    /**
     * @param id entity ID.
     * @return entity.
     */
    @SuppressWarnings("unchecked")
    public T get(final Key id) {
        final Element e = cache.get(id);
        if (e != null) {
            return (T) e.getObjectValue();
        }
        return null;
    }
    /**
     * @param id entity ID.
     */
    public void remove(final Key id) {
        cache.remove(id);
    }

    /**
     * Cleares the cache.
     */
    public void clear() {
        cache.removeAll();
    }
}
