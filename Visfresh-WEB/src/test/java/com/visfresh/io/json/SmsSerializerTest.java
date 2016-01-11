/**
 *
 */
package com.visfresh.io.json;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import com.google.gson.JsonObject;
import com.visfresh.sms.SmsMessage;
/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class SmsSerializerTest {
    private SmsSerializer serializer;

    /**
     * Default constructor.
     */
    public SmsSerializerTest() {
        super();
    }

    @Before
    public void setUp() {
        serializer = new SmsSerializer();
    }

    /**
     * Tests message serializing
     */
    @Test
    public void testSmsMessage() {
        final String firstPhone = "1111111118";
        final String secondPhone = "1111111119";
        final String message = "SMS message";
        final String subject = "SMS subject";

        SmsMessage m = new SmsMessage();
        m.setPhones(new String[]{firstPhone, secondPhone});
        m.setMessage(message);
        m.setSubject(subject);

        final JsonObject json = serializer.toJson(m);
        m = serializer.parseSmsMessage(json);

        assertEquals(firstPhone, m.getPhones()[0]);
        assertEquals(secondPhone, m.getPhones()[1]);
        assertEquals(message, m.getMessage());
        assertEquals(subject, m.getSubject());
    }
}
