/**
 *
 */
package com.visfresh.l12n;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.ResourceBundle.Control;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class XmlControl extends Control {
    /**
     * The instance of control.
     */
    public static final XmlControl INSTANCE = new XmlControl();

    /**
     * Default constructor.
     */
    private XmlControl() {
        super();
    }

    /**
     * Gets the formats.
     *
     * @param baseName the base name
     * @return the formats
     * @see java.util.ResourceBundle.Control#getFormats(java.lang.String)
     */
    @Override
    public List<String> getFormats(final String baseName) {
        if (baseName == null) {
            throw new NullPointerException();
        }
        return Arrays.asList("xml");
    }

    /* (non-Javadoc)
     * @see java.util.ResourceBundle.Control#newBundle(java.lang.String, java.util.Locale, java.lang.String, java.lang.ClassLoader, boolean)
     */
    /**
     * New bundle.
     *
     * @param baseName the base name
     * @param locale the locale
     * @param format the format
     * @param loader the loader
     * @param reload the reload
     * @return the resource bundle
     * @throws IllegalAccessException the illegal access exception
     * @throws InstantiationException the instantiation exception
     * @throws IOException Signals that an I/O exception has occurred.
     * @see java.util.ResourceBundle.Control#newBundle(java.lang.String, java.util.Locale, java.lang.String, java.lang.ClassLoader, boolean)
     */
    @Override
    public ResourceBundle newBundle(final String baseName, final Locale locale,
            final String format, final ClassLoader loader, final boolean reload)
            throws IllegalAccessException, InstantiationException, IOException {
        if (baseName == null || locale == null || format == null
                || loader == null)
            throw new NullPointerException();
        ResourceBundle bundle = null;
        if (format.equals("xml")) {
            final String bundleName = toBundleName(baseName, locale);
            URL url = loader.getResource("messages/" + toResourceName(bundleName, format));
            if (url == null) {
                url = loader.getResource("messages/" + toResourceName(baseName, format));
            }

            if (url != null) {
                final URLConnection connection = url.openConnection();
                if (connection != null) {
                    if (reload) {
                        // Disable caches to get fresh data for
                        // reloading.
                        connection.setUseCaches(false);
                    }
                    final InputStream in = connection.getInputStream();
                    try {
                        final BufferedInputStream bis = new BufferedInputStream(in);
                        bundle = new XmlResourceBundle(bis);
                    } finally {
                        in.close();
                    }
                }
            }
        }

        return bundle;
    }
}
