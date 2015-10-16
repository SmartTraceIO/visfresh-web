/**
 *
 */
package com.visfresh.services;

import org.junit.AfterClass;
import org.junit.Test;
import org.springframework.context.support.AbstractApplicationContext;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class RestServiceTest {
    private static AbstractApplicationContext context;
    /**
     * Service to test
     */
    private RestService service;
    /**
     * Default constructor.
     */
    public RestServiceTest() {
        super();
    }

    @Test
    public void test1() {
        System.out.println(service);
    }

    @AfterClass
    public static void destroyStatics() {
        if (context != null) {
            context.destroy();
        }
    }
}
