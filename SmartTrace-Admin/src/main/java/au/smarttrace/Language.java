/**
 *
 */
package au.smarttrace;

import java.util.Locale;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public enum Language {
    English(Locale.US),
    German(Locale.GERMAN);

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
