/**
 *
 */
package com.visfresh.cfg;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public final class Config {
    private static final Map<String, String> properties = loadProperties();

    /**
     * Default constructor.
     */
    private Config() {
        super();
    }

    /**
     * @return map of configuration properties.
     */
    private static Map<String, String> loadProperties() {
        final Properties props = new Properties();

        try {
            final Reader r = new InputStreamReader(Config.class.getClassLoader().getResourceAsStream("app.properties"), "UTF-8");
            try {
                props.load(r);
            } finally {
                r.close();
            }
        } catch (final IOException e) {
            throw new RuntimeException("Failed to load configuration properties", e);
        }

        final Map<String, String> map = new HashMap<>();
        for (final Object objKey : props.keySet()) {
            final String key = (String) objKey;
            map.put(key, props.getProperty(key));
        }
        return map;
    }

    public static String getProperty(final String key) {
        return properties.get(key);
    }
    /**
     * @param key
     * @param defaultValue
     * @return
     */
    public static String getProperty(final String key, final String defaultValue) {
        final String value = getProperty(key);
        return value == null ? defaultValue : value;
    }
}
