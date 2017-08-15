/**
 *
 */
package com.visfresh.dao.impl;

import javax.annotation.PostConstruct;

import org.springframework.stereotype.Component;

import net.sf.ehcache.CacheManager;
import net.sf.ehcache.config.Configuration;
import net.sf.ehcache.config.ConfigurationFactory;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class CacheManagerHolder {
    private CacheManager cacheManager;

    /**
     * Default constructor.
     */
    public CacheManagerHolder() {
        super();
    }
    @PostConstruct
    public void init() {
        if (cacheManager == null) {
            //create unique cache name.
            final String name = "CacheHolder-" + System.identityHashCode(this);

            //create cache manager with given name.
            final Configuration cfg = ConfigurationFactory.parseConfiguration();
            cfg.setName(name);
            cacheManager = CacheManager.newInstance(cfg);
        }
    }
    /**
     * @param cm
     */
    public CacheManagerHolder(final CacheManager cm) {
        this.cacheManager = cm;
    }

    /**
     * @return the cacheManager
     */
    public CacheManager getCacheManager() {
        return cacheManager;
    }
}
