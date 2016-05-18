/**
 *
 */
package com.visfresh.checkavailability;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public final class Main {

    /**
     * Default constructor.
     */
    private Main() {
        super();
    }

    public static void main(final String[] args) {
        new Checker().check();
    }
}
