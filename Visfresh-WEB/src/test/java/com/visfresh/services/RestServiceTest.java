/**
 *
 */
package com.visfresh.services;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;

import com.visfresh.junit.jpa.JUnitJpaConfig;
import com.visfresh.mpl.services.RestServiceImpl;

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

    @BeforeClass
    public static void staticInit() {
        final AnnotationConfigWebApplicationContext ctxt = new AnnotationConfigWebApplicationContext();
        ctxt.scan(JUnitJpaConfig.class.getPackage().getName(),
                RestServiceImpl.class.getPackage().getName());
        ctxt.refresh();
        context = ctxt;
    }

    @Before
    public void setUp() {
        service = context.getBean(RestService.class);
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
