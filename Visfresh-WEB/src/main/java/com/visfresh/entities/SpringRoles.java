/**
 *
 */
package com.visfresh.entities;

/**
 * This class provides support by spring roles which requires ROLE_ prefix. Used in controller annotations, because
 * annotations accepting only constants as arguments.
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public final class SpringRoles {
    public static final String SPRING_ROLE_PREFIX = "ROLE_";

    public static final String SmartTraceAdmin = SPRING_ROLE_PREFIX + "SmartTraceAdmin";
    public static final String Admin = SPRING_ROLE_PREFIX + "Admin";
    public static final String BasicUser = SPRING_ROLE_PREFIX + "BasicUser";
    public static final String NormalUser = SPRING_ROLE_PREFIX + "NormalUser";

    /**
     * Default constructor.
     */
    private SpringRoles() {
        super();
    }

}
