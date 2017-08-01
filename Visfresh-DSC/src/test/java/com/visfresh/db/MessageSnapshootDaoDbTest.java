/**
 *
 */
package com.visfresh.db;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import com.visfresh.spring.mock.JUnitConfig;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class MessageSnapshootDaoDbTest extends MessageSnapshootDao {
    /**
     * Spring context.
     */
    private AnnotationConfigApplicationContext spring;

    /**
     * Default constructor.
     */
    public MessageSnapshootDaoDbTest() {
        super();
    }

    @Before
    public void setUp() {
        spring = JUnitConfig.createContext();
        jdbc = spring.getBean(NamedParameterJdbcTemplate.class);
    }

    @Test
    public void testDoSaveReceived() {
        final String imei = "123456789";
        final String signature = "987654321";

        assertTrue(doSaveReceived(imei, signature));
        assertFalse(doSaveReceived(imei, signature));
        assertTrue(doSaveReceived(imei, signature + "..."));
    }

    @After
    public void tearDown() throws Exception {
        //clean up data base
        jdbc.update("delete from " + TABLE, new HashMap<String, Object>());
        spring.close();
    }
}
