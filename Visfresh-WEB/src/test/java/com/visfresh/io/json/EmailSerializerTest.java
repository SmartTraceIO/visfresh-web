/**
 *
 */
package com.visfresh.io.json;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import com.google.gson.JsonObject;
import com.visfresh.io.email.EmailMessage;
/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class EmailSerializerTest {
    private EmailSerializer serializer;

    /**
     * Default constructor.
     */
    public EmailSerializerTest() {
        super();
    }

    @Before
    public void setUp() {
        serializer = new EmailSerializer();
    }

    /**
     * Tests message serializing
     */
    @Test
    public void testEmailMessage() {
        final String firstEmail = "first@e.mail";
        final String secondEmail = "second@e.mail";
        final String message = "Email message";
        final String subject = "Email subject";

        EmailMessage m = new EmailMessage();
        m.setEmails(new String[]{firstEmail, secondEmail});
        m.setMessage(message);
        m.setSubject(subject);

        final JsonObject json = serializer.toJson(m);
        System.out.println(json);
        m = serializer.parseEmailMessage(json);

        assertEquals(firstEmail, m.getEmails()[0]);
        assertEquals(secondEmail, m.getEmails()[1]);
        assertEquals(message, m.getMessage());
        assertEquals(subject, m.getSubject());
    }
}
