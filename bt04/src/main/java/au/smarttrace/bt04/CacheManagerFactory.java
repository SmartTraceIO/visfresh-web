/**
 *
 */
package au.smarttrace.bt04;

import java.time.Duration;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.ehcache.Cache;
import org.ehcache.CacheManager;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.ehcache.config.builders.ExpiryPolicyBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.springframework.stereotype.Component;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class CacheManagerFactory {
    /**
     * Cache manager.
     */
    private CacheManager cacheManager;

    /**
     * Default constructor.
     */
    public CacheManagerFactory() {
        super();
    }

    @PostConstruct
    public void init() {
        final CacheManagerBuilder<CacheManager> builder = CacheManagerBuilder.newCacheManagerBuilder();
        cacheManager = builder.build(true);
    }
    @PreDestroy
    public void destroy() {
        cacheManager.close();
    }
    /**
     * @return the cacheManager
     */
    public CacheManager getCacheManager() {
        return cacheManager;
    }

    public static void main(final String[] args) throws InstantiationException, IllegalAccessException, ClassNotFoundException {
        final CacheManagerFactory f = new CacheManagerFactory();
        f.init();

        final CacheManager mgr = f.getCacheManager();
        final CacheConfigurationBuilder<String, String> b = CacheConfigurationBuilder.newCacheConfigurationBuilder(
                String.class, String.class,
                ResourcePoolsBuilder.heap(1000))
                .withExpiry(ExpiryPolicyBuilder.timeToLiveExpiration(Duration.ofSeconds(30)))
                .withExpiry(ExpiryPolicyBuilder.timeToIdleExpiration(Duration.ofSeconds(15)));

        final Cache<String, String> c = mgr.createCache("tmp", b.build());
        c.put("key", "any-cached-value");
        System.out.println(c.get("key"));

        mgr.close();
    }
}
