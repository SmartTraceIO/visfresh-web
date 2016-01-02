/**
 *
 */
package com.visfresh.entities;

import java.util.Locale;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public enum Language {
    English(Locale.US);

    private Locale locale;

    private Language(final Locale locale) {
        this.locale = locale;
    }
    /**
     * @return the locale
     */
    public Locale getLocale() {
        return locale;
    }
}
