/**
 *
 */
package com.visfresh.tools;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public final class IbmIcuTimeZoneTest {
    private static final String TIMEZONE_ID_PREFIXES = "^(America|Asia|Atlantic|Australia|Europe|Indian|Pacific)/.*";

    private static List<TimeZone> timeZones = initTimeZones();

    /**
     * Default constructor.
     */
    private IbmIcuTimeZoneTest() {
        super();
    }

    public static List<TimeZone> getTimeZones() {
        return timeZones;
    }

    private static ArrayList<TimeZone> initTimeZones() {
        final ArrayList<TimeZone> timeZones = new ArrayList<TimeZone>();
        final List<String> timeZoneIds = new LinkedList<String>(
                Arrays.asList(TimeZone.getAvailableIDs()));

        for (final String id : timeZoneIds) {
            if (id.matches(TIMEZONE_ID_PREFIXES)) {
                timeZones.add(TimeZone.getTimeZone(id));
            }
        }
        sort(timeZones);
        return timeZones;
    }
    /**
     * @param timeZones
     */
    private static void sort(final List<TimeZone> timeZones) {
        Collections.sort(timeZones, new Comparator<TimeZone>() {
            @Override
            public int compare(final TimeZone a, final TimeZone b) {
                int result = 0;
                if (result == 0) {
                    final String sega = getFirstSegment(a.getID());
                    final String segb = getFirstSegment(b.getID());
                    result = sega.compareTo(segb);
                }
                if (result == 0) {
                    result = new Integer(a.getRawOffset()).compareTo(b.getRawOffset());
                }
                if (result == 0) {
                    result = a.getID().compareTo(b.getID());
                }
                return result;
            }
        });
    }

    /**
     * @param id
     * @return
     */
    protected static String getFirstSegment(final String id) {
        return id.split(Pattern.quote("/"))[0];
    }

    public static String displayTimeZone(final int rawOffset) {
        final long hours = TimeUnit.MILLISECONDS.toHours(rawOffset);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(rawOffset)
                - TimeUnit.HOURS.toMinutes(hours);
        // avoid -4:-30 issue
        minutes = Math.abs(minutes);

        String result;
        if (hours >= 0) {
            result = String.format("GMT+%d:%02d", hours, minutes);
        } else {
            result = String.format("GMT%d:%02d", hours, minutes);
        }

        return result;
    }

    public static void main(final String[] args) {
        final List<String> ids = new LinkedList<String>(Arrays.asList(TimeZone.getAvailableIDs()));

        for (final TimeZone tz : getTimeZones()) {
            System.out.println(
                    "("+ displayTimeZone(tz.getRawOffset()) + ") "
                    + tz.getID() + " "
                    + tz.getDisplayName(Locale.US)
                    + ", observes daylight time " + tz.observesDaylightTime());
        }
        System.out.println("Number of time zones = " + ids.size());
    }
}
