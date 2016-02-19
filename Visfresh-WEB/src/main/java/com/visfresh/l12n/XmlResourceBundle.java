/**
 *
 */
package com.visfresh.l12n;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.InvalidPropertiesFormatException;
import java.util.Iterator;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.Set;


/**
 * The Class XMLResourceBundle.
 *
 * @author Vyacheslav Soldatov <vsoldatov@altpayusa.com>
 */
public class XmlResourceBundle extends ResourceBundle {
    /**
     * The properties.
     */
    private final Properties properties =  new Properties();

    /**
     * Instantiates a new XML resource bundle.
     *
     * @param in properties stream.
     * @throws InvalidPropertiesFormatException the invalid properties format exception
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public XmlResourceBundle(final InputStream in) throws InvalidPropertiesFormatException, IOException {
        super();
        this.properties.loadFromXML(in);
        final Iterator<Object> keys = new HashSet<>(properties.keySet()).iterator();
        //remove white spaces
        while (keys.hasNext()) {
            final String key = (String) keys.next();
            final String value = properties.getProperty(key).trim();

            final String[] split = value.split("\n");
            if (split.length > 1) {
                final StringBuilder sb = new StringBuilder();
                for (final String s : split) {
                    if (sb.length() > 0) {
                        sb.append('\n');
                    }
                    sb.append(s.trim());
                }

                properties.setProperty(key, sb.toString());
            }
        }
    }

    /* (non-Javadoc)
     * @see java.util.ResourceBundle#handleGetObject(java.lang.String)
     */
    /**
     * Handle get object.
     *
     * @param key the key
     * @return the object
     * @see java.util.ResourceBundle#handleGetObject(java.lang.String)
     */
    @Override
    protected Object handleGetObject(final String key) {
        return properties.getProperty(key);
    }

    /* (non-Javadoc)
     * @see java.util.ResourceBundle#getKeys()
     */
    /**
     * Gets the keys.
     *
     * @return the keys
     * @see java.util.ResourceBundle#getKeys()
     */
    @Override
    public Enumeration<String> getKeys() {
        final Set<String> names = new HashSet<String>();
        final Enumeration<?> e = properties.propertyNames();
        while (e.hasMoreElements()) {
            names.add((String) e.nextElement());
        }
        return Collections.enumeration(names);
    }
}
