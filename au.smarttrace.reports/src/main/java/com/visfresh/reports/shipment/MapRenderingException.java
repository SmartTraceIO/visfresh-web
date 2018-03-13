/**
 *
 */
package com.visfresh.reports.shipment;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class MapRenderingException extends RuntimeException {
    private static final long serialVersionUID = 1124431166702123150L;

    /**
     * @param cause cause exception
     */
    public MapRenderingException(final Throwable cause) {
        super(cause);
    }
    /**
     * @param cause exception message
     * @param cause cause exception
     */
    public MapRenderingException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
