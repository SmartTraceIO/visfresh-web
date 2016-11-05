/**
 *
 */
package com.visfresh.dao.impl;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public enum TimeAtom {
    Week,
    Month,
    Quarter;
    public static TimeAtom getAtom(final String str) {
        for (final TimeAtom a : values()) {
            if (a.name().equalsIgnoreCase(str)) {
                return a;
            }
        }
        throw new IllegalArgumentException("Not enum constant found for " + str);
    }
}
