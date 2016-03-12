/**
 *
 */
package com.visfresh.utils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import com.visfresh.entities.User;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public final class DateTimeUtils {
    /**
     * Default constructor.
     */
    private DateTimeUtils() {
        super();
    }

    /**
     * @param user user.
     * @param format date format string.
     * @return date format.
     */
    public static DateFormat createDateFormat(final User user, final String format) {
        final DateFormat fmt = new SimpleDateFormat(format, user.getLanguage().getLocale());
        fmt.setTimeZone(user.getTimeZone());
        return fmt;
    }
    /**
     * @param user user.
     * @return date format.
     */
    public static DateFormat createPrettyFormat(final User user) {
        return DateTimeUtils.createDateFormat(user, "h:mmaa d MMM yyyy");
    }
    /**
     * @param user user.
     * @return date format.
     */
    public static DateFormat createIsoFormat(final User user) {
        return DateTimeUtils.createDateFormat(user, "yyyy-MM-dd HH:mm");
    }
}
