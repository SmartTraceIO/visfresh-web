/**
 *
 */
package au.st.messaging;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import junit.framework.AssertionFailedError;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class SystemMessageDispatcherTest extends MockSystemMessageDispatcher {
    private final Map<String, Date> activities = new HashMap<>();
    private List<SystemMessage> messages = new LinkedList<>();
    private ObjectMapper serializer;
    private final Map<Long, String> locks = new HashMap<>();
    private UnlockerService unlockService;

    /**
     * Default constructor.
     */
    public SystemMessageDispatcherTest() {
        super();
    }

    @Before
    public void setUp() {
        //create message serializer
        final ObjectMapper m = new ObjectMapper();
        m.setDateFormat(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ"));
        m.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        m.setVisibility(PropertyAccessor.ALL, Visibility.NONE);
        m.setVisibility(PropertyAccessor.FIELD, Visibility.ANY);
        this.serializer = m;

        //set up DAOs
        final MockDispatcherIdDao dispatcherIdDao = new MockDispatcherIdDao(activities);
        setDispatcherIdDao(dispatcherIdDao);

        final MockSystemMessageDao messageDao = new MockSystemMessageDao(messages, locks);
        setMessageDao(messageDao);

        //configure unlock service
        unlockService = new UnlockerService();
        unlockService.setDispatcherIdDao(dispatcherIdDao);
        unlockService.setSystemMessageDao(messageDao);
        unlockService.setGroupLockDao(new MockGroupLockDao());

        start();
    }
    @Test
    public void testIncorrectCorePoolSize() throws InterruptedException {
        stop();
        setCorePoolSize(0);

        try {
            start();
            throw new AssertionFailedError("Incorrect core pool size should be detected");
        } catch (final Exception e) {
            // correct
        }
    }
    @Test
    public void testIncorrectMaxPoolSize() throws InterruptedException {
        stop();
        setCorePoolSize(4);
        setMaxPoolSize(3);

        try {
            start();
            throw new AssertionFailedError("Incorrect max pool size should be detected");
        } catch (final Exception e) {
            // correct
        }
    }
    @Test
    public void testIncorrectKeepAlivetime() throws InterruptedException {
        stop();
        setKeepAliveTime(-1);

        try {
            start();
            throw new AssertionFailedError("Incorrect keep alive time should be detected");
        } catch (final Exception e) {
            // correct
        }
    }
    @Test
    public void testIncorrectMaxThreadTimeOut() throws InterruptedException {
        stop();
        setMaxInactiveTime(-1);

        try {
            start();
            throw new AssertionFailedError("Incorrect max thread time out should be detected");
        } catch (final Exception e) {
            // correct
        }
    }
    @Test
    public void testNullSystemMessageDao() throws InterruptedException {
        stop();
        setMessageDao(null);

        try {
            start();
            throw new AssertionFailedError("Null system message DAO should be detected");
        } catch (final Exception e) {
            // correct
        }
    }
    @Test
    public void testDispatcherIdDao() throws InterruptedException {
        stop();
        setDispatcherIdDao(null);

        try {
            start();
            throw new AssertionFailedError("Null dispatcher ID dao should be detected");
        } catch (final Exception e) {
            // correct
        }
    }
    @Test
    public void testLockMessageOnDispatch() {
        messages.add(createTestMessage());

        final AtomicBoolean isMessageLocked = new AtomicBoolean();
        final AtomicReference<TestMessage> processedMessage = new AtomicReference<>();

        addTestMessageHandler(m -> {
            //check message is looked
            final Set<String> listenTypes = new HashSet<>();
            listenTypes.add(TestMessageHandler.TYPE);
            isMessageLocked.set(locks.size() > 0);

            processedMessage.set(m);
        });

        executeWorker();

        assertTrue(isMessageLocked.get());
        assertNotNull(processedMessage.get());
    }
    @Test
    public void testNotProcessMessagesOfUnsupportedTypes() {
        final SystemMessage msg = createTestMessage();
        msg.setType("UnsupportedType");
        messages.add(msg);

        final AtomicReference<TestMessage> processedMessage = new AtomicReference<>();

        addTestMessageHandler(m -> {
            //this should not be invoked
            processedMessage.set(m);
        });

        executeWorker();

        assertNull(processedMessage.get());
    }
    @Test
    public void testUnlockMessageAfterDispatchMessaged() {
        messages.add(createTestMessage());
        addTestMessageHandler(m -> {});

        executeWorker();

        assertNull(locks.get(getId()));
    }
    @Test
    public void testNotUnlockMessageAfterRetryableException() {
        messages.add(createTestMessage());
        addTestMessageHandler(m -> {
            final SystemMessageException e = new SystemMessageException();
            e.setRetryOn(new Date(System.currentTimeMillis() + 1000000l));
            throw e;
        });

        executeWorker();

        assertEquals(1, messages.size());
        assertNull(locks.get(getId()));
    }
    /**
     * After have retry exception for message, it not unlocked. Need to check for next
     * iteration dispatcher will not select given message (same dispatcher which have dispatched
     * it in previous iteration)
     */
    @Test
    public void testNotTouchLockedBlockedMessage() {
        messages.add(createTestMessage());
        addTestMessageHandler(m -> {
            final SystemMessageException e = new SystemMessageException();
            e.setRetryOn(new Date(System.currentTimeMillis() + 1000000l));
            throw e;
        });

        executeWorker();

        assertFalse(this.hasDataToProcess());
    }
    @Test
    public void testMaxRetryNumber() {
        final SystemMessage msg = createTestMessage();
        messages.add(msg);
        final TestMessageHandler handler = new TestMessageHandler() {
            @Override
            public void handle(final TestMessage m) throws SystemMessageException {
                final SystemMessageGroupException e = new SystemMessageGroupException();
                e.setRetryOn(new Date());
                throw e;
            }
            /* (non-Javadoc)
             * @see au.st.messaging.TestMessageHandler#getMaxNumberOfRetry()
             */
            @Override
            public int getMaxNumberOfRetry() {
                return 3;
            }
        };
        addTestMessageHandler(handler);

        executeWorker();
        assertFalse(this.hasDataToProcess());
        assertEquals(3, msg.getNumberOfRetry());
    }

    public void addTestMessageHandler(final TestMessageHandler h) {
        addMessageHandler(h);
    }

    /**
     * @return system message with test message.
     */
    private SystemMessage createTestMessage() {
        final SystemMessage msg = new SystemMessage();
        msg.setType(TestMessageHandler.TYPE);
        msg.setId(99l);
        msg.setRetryOn(new Date());
        msg.setTime(msg.getRetryOn());

        final TestMessage tm = new TestMessage();
        tm.setIntValue(77);
        tm.setStringVlue("StringValue");

        try {
            msg.setPayload(serializer.writeValueAsString(tm));
        } catch (final JsonProcessingException e) {
            e.printStackTrace();
        }

        return msg;
    }
}
