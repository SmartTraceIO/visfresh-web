/**
 *
 */
package com.visfresh.constants;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public final class ErrorCodes {
    public static final int AUTHENTICATION_ERROR = 1;
    public static final int SECURITY_ERROR = 2;
    public static final int INVALID_JSON = 3;
    public static final int INCORRECT_REQUEST_DATA = 4;
    /**
     * The entity to delete is referenced by another entity and can't be deleted.
     */
    public static final int ENTITY_IN_USE = 5;

    /**
     * Default constructor.
     */
    private ErrorCodes() {
        super();
    }
}
