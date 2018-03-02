/**
 *
 */
package com.visfresh.utils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import com.visfresh.dao.impl.TimeAtom;
import com.visfresh.dao.impl.TimeRanges;
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
        return DateTimeUtils.createDateFormat("H:mm d MMM yyyy", lang, tz);
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
        final TimeZone timeZone = company.getTimeZone();
        final Language language = company.getLanguage();

        final TimeZone tz = timeZone != null? timeZone : TimeZone.getTimeZone("UTC");
        final Locale locale = language != null ? language.getLocale() : Locale.ENGLISH;
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
        final SimpleDateFormat fmt = new SimpleDateFormat("H:mm dMMMyyyy ", locale);
        fmt.setTimeZone(tz);

        return fmt.format(date) + getTimeZoneString(rawOffset);
    }

    /**
     * @param rawOffset
     * @return
     */
    public static String getTimeZoneString(final int rawOffset) {
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
    /**
     * @param time
     * @param timeZone
     * @return
     */
    public static Date convertToTimeZone(final Date time, final TimeZone timeZone) {
        final long t = time.getTime();
        return new Date(t - (TimeZone.getDefault().getOffset(t) - timeZone.getOffset(t)));
    }
    /**
     * @param time
     * @param timeZone
     * @return
     */
    public static Date convertFromTimeZone(final Date time, final TimeZone timeZone) {
        final long t = time.getTime();
        return new Date(t + (TimeZone.getDefault().getOffset(t) - timeZone.getOffset(t)));
    }
    /**
     * @param t
     * @return
     */
    public static TimeRanges getTimeRanges(final long t, final TimeAtom timeAtom) {
        final TimeRanges r = new TimeRanges();

        final Calendar calendar = new GregorianCalendar();
        calendar.setTimeInMillis(t);

        //start range
        switch(timeAtom) {
            case Month:
                calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMinimum(Calendar.DAY_OF_MONTH));
                break;
            case Quarter:
                final int month = calendar.get(Calendar.MONTH);
                final int quarter = month / 3;

                final int startMonth = quarter * 3;
                calendar.set(Calendar.MONTH, startMonth);
                calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMinimum(Calendar.DAY_OF_MONTH));
                break;
            case Week:
                final int day = getDayFromMonday(calendar);
                if (day != 0) {
                    calendar.add(Calendar.DAY_OF_YEAR, -day);
                }
                break;
                default:
                    throw new RuntimeException("Unexpected time atom: " + timeAtom);
        }

        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 1);
        r.setStartTime(calendar.getTimeInMillis());

        //end range
        calendar.setTimeInMillis(t);
        switch(timeAtom) {
            case Month:
                calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
                break;
            case Quarter:
                final int month = calendar.get(Calendar.MONTH);
                final int quarter = month / 3;

                final int startMonth = quarter * 3 + 2;
                calendar.set(Calendar.MONTH, startMonth);
                calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
                break;
            case Week:
                final int day = getDayFromMonday(calendar);
                if (day != 6) {
                    calendar.add(Calendar.DAY_OF_YEAR, 6 - day);
                }
                break;
                default:
                    throw new RuntimeException("Unexpected time atom: " + timeAtom);
        }

        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);
        r.setEndTime(calendar.getTimeInMillis());

        return r;
    }

    /**
     * @param calendar
     * @return
     */
    private static int getDayFromMonday(final Calendar calendar) {
        int day = calendar.get(Calendar.DAY_OF_WEEK) - Calendar.MONDAY;
        if (day < 0) {
            day = 7 + day;
        }
        return day;
    }
}
