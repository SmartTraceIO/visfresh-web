/**
 *
 */
package com.visfresh.db;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.junit.Test;

import com.visfresh.DeviceMessage;
import com.visfresh.DeviceMessageType;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class MessageSnapshootDaoTest extends MessageSnapshootDao {
    /**
     * Default constructor.
     */
    public MessageSnapshootDaoTest() {
        super();
    }
    @Test
    public void testCreateSignature() {
        final List<DeviceMessage> messages = new LinkedList<>();
        final DeviceMessage m = createMessage();
        messages.add(m);

        String sig1 = createSignature(messages);

        assertEquals(sig1, createSignature(messages));

        //test change message
        m.setMessage("new message");
        assertNotSame(sig1, createSignature(messages));

        //test change size
        sig1 = createSignature(messages);
        messages.add(m);
        assertNotSame(sig1, createSignature(messages));
    }
    /**
     * @return
     */
    private DeviceMessage createMessage() {
        final DeviceMessage msg = new DeviceMessage();
        msg.setBattery(12);
        msg.setId(777l);
        msg.setImei("123455789");
        msg.setMessage("message");
        msg.setTemperature(15.);
        msg.setType(DeviceMessageType.BRT);
        msg.setTime(new Date());
        msg.setTypeString("BRT");
        return msg;
    }
}
