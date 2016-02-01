/**
 *
 */
package com.visfresh.dispatcher;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import com.visfresh.DeviceMessage;
import com.visfresh.DeviceMessageType;
import com.visfresh.db.MessageDao;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class AbstractDispatcherTest extends TestCase {

    private ClassPathXmlApplicationContext spring;
    private JunitDispatcher dispatcher;
    private MessageDao dao;
    private NamedParameterJdbcTemplate jdbcTemplate;

    /**
     * Default constructor.
     */
    public AbstractDispatcherTest() {
        super();
    }

    /**
     * @param name test case name.
     */
    public AbstractDispatcherTest(final String name) {
        super(name);
    }

    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    @Override
    protected void setUp() throws Exception {
        spring = new ClassPathXmlApplicationContext("application-context-junit.xml");
        dispatcher = spring.getBean(JunitDispatcher.class);
        dao = spring.getBean(MessageDao.class);
        jdbcTemplate = spring.getBean(NamedParameterJdbcTemplate.class);
    }

    public void testHandleSuccess() {
        final DeviceMessage msg = createMessage();
        dao.create(msg);

        dispatcher.handleSuccess(msg);
        final List<Map<String, Object>> rows = jdbcTemplate.queryForList("select * from "
                + MessageDao.DEVICE_MESSAGES_TABLE,
                new HashMap<String, Object>());

        assertEquals(0, rows.size());
    }
    public void testHandleRetryableError() {
        final DeviceMessage msg = createMessage();
        dao.create(msg);

        dispatcher.setRetryLimit(1);

        dispatcher.handleError(msg, new RetryableException());
        assertEquals(1, jdbcTemplate.queryForList("select * from "
                + MessageDao.DEVICE_MESSAGES_TABLE,
                new HashMap<String, Object>()).size());

        dispatcher.handleError(msg, new RetryableException());
        assertEquals(0, jdbcTemplate.queryForList("select * from "
                + MessageDao.DEVICE_MESSAGES_TABLE,
                new HashMap<String, Object>()).size());
    }
    public void testHandleNotRetryableError() {
        final DeviceMessage msg = createMessage();
        dao.create(msg);

        dispatcher.setRetryLimit(1);
        dispatcher.handleError(msg, new Exception());

        assertEquals(0, jdbcTemplate.queryForList("select * from "
                + MessageDao.DEVICE_MESSAGES_TABLE,
                new HashMap<String, Object>()).size());
    }
    /**
     * @return message.
     */
    private DeviceMessage createMessage() {
        final DeviceMessage msg = new DeviceMessage();
        msg.setImei("12345");
        msg.setType(DeviceMessageType.DRK);
        msg.setTime(new Date());
        return msg;
    }

    /* (non-Javadoc)
     * @see junit.framework.TestCase#tearDown()
     */
    @Override
    protected void tearDown() throws Exception {
        //clean up data base
        jdbcTemplate.update("delete from " + MessageDao.DEVICE_MESSAGES_TABLE,
                new HashMap<String, Object>());
        spring.close();
    }
}
