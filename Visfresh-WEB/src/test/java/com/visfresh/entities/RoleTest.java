/**
 *
 */
package com.visfresh.entities;

import static org.junit.Assert.assertTrue;

import java.util.HashSet;

import org.junit.Test;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class RoleTest {
    /**
     * Default constructor.
     */
    public RoleTest() {
        super();
    }

    @Test
    public void testSmartTraceAdmin() {
        final User user = new User();
        user.setRoles(new HashSet<Role>());
        user.getRoles().add(Role.SmartTraceAdmin);

        for (final Role r : Role.values()) {
            assertTrue(r.hasRole(user));
        }
    }
    @Test
    public void testAdmin() {
        final User user = new User();
        user.setRoles(new HashSet<Role>());
        user.getRoles().add(Role.Admin);

        for (final Role r : Role.values()) {
            if (r != Role.SmartTraceAdmin) {
                assertTrue(r.hasRole(user));
            }
        }
    }
}
