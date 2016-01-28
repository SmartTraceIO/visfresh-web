/**
 *
 */
package com.visfresh.utils;

import java.util.Map;
import java.util.ResourceBundle;

import com.visfresh.l12n.XmlControl;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public final class Messages {
    /**
     * Default constructor.
     */
    private Messages() {
        super();
    }

    /**
     * @param key bundle key.
     * @param replacements replacements map.
     * @return message string.
     */
    public static String getMessage(final String key, final Map<String, String> replacements) {
        final ResourceBundle bundle = ResourceBundle.getBundle("messages", XmlControl.INSTANCE);
        return StringUtils.getMessage(bundle.getString(key), replacements);
    }
}
