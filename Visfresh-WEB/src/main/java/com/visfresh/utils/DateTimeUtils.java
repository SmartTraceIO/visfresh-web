/**
 *
 */
package com.visfresh.utils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import com.visfresh.entities.Company;
import com.visfresh.entities.Language;

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
    public static DateFormat createDateFormat(final String format, final Language lang, final TimeZone tz) {
        final DateFormat fmt = new SimpleDateFormat(format, lang.getLocale());
        fmt.setTimeZone(tz);
        return fmt;
    }
    /**
     * @param user user.
     * @return date format.
     */
    public static DateFormat createPrettyFormat(final Language lang, final TimeZone tz) {
        return DateTimeUtils.createDateFormat("h:mmaa d MMM yyyy", lang, tz);
    }
    /**
     * @param user user.
     * @return date format.
     */
    public static DateFormat createIsoFormat(final Language lang, final TimeZone tz) {
        return DateTimeUtils.createDateFormat("yyyy-MM-dd' 'HH:mm", lang, tz);
    }
    /**
     * @param company
     * @param date
     * @return
     */
    public static String formatShipmentDate(final Company company, final Date date) {
        final TimeZone tz = company.getTimeZone() != null? company.getTimeZone() : TimeZone.getTimeZone("UTC");
        final Locale locale = company.getLanguage() != null ? company.getLanguage().getLocale() : Locale.ENGLISH;
        return formatDate(date, tz, locale);
    }

    /**
     * @param date
     * @param tz
     * @param locale
     * @return
     */
    private static String formatDate(final Date date, final TimeZone tz,
            final Locale locale) {
        final int rawOffset = tz.getRawOffset();
        final SimpleDateFormat fmt = new SimpleDateFormat("h:mmaa dMMMyyyy ", locale);
        fmt.setTimeZone(tz);

        return fmt.format(date) + getTimeZoneString(rawOffset);
    }

    /**
     * @param rawOffset
     * @return
     */
    protected static String getTimeZoneString(final int rawOffset) {
        if (rawOffset == 0) {
            return "";
        }

        //time zone
        final long hours = TimeUnit.MILLISECONDS.toHours(rawOffset);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(rawOffset)
                - TimeUnit.HOURS.toMinutes(hours);
        // avoid -4:-30 issue
        minutes = Math.abs(minutes);

        String tzString;
        if (hours >= 0) {
            if (minutes > 0) {
                tzString = String.format("+%d:%d", hours, minutes);
            } else {
                tzString = String.format("+%d", hours, minutes);
            }
        } else {
            if (minutes > 0) {
                tzString = String.format("%d:%d", hours, minutes);
            } else {
                tzString = String.format("%d", hours, minutes);
            }
        }
        return tzString;
    }
}
