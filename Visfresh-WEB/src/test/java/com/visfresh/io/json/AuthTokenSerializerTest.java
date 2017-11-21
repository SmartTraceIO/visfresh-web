/**
 *
 */
package com.visfresh.io.json;

import static org.junit.Assert.assertEquals;

import java.util.Date;
import java.util.TimeZone;

import org.junit.Before;
import org.junit.Test;

import com.visfresh.services.AuthToken;

import junit.framework.AssertionFailedError;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class AuthTokenSerializerTest {
    /**
     * The serializer to test.
     */
    private AuthTokenSerializer serializer;

    /**
     * Default constructor.
     */
    public AuthTokenSerializerTest() {
        super();
    }

    @Before
    public void setUp() {
        serializer = new AuthTokenSerializer(TimeZone.getDefault());
    }

    @Test
    public void testSerialize() {
        final String value = "junit-9032584709238457";
        final String client = "junit93257";
        final Date creationTime = new Date(System.currentTimeMillis() - 429802735487l);
        final Date expirationTime = new Date(creationTime.getTime() + 60 * 60 * 1000l);

        AuthToken token = new AuthToken(value);
        token.setClientInstanceId(client);
        token.setExpirationTime(expirationTime);

        token = serializer.parseAuthToken(serializer.toJson(token));

        assertEquals(value, token.getToken());
        assertEquals(client, token.getClientInstanceId());
        assertEqualsDates(expirationTime, token.getExpirationTime());
    }

    /**
     * @param d1 first date to compare.
     * @param d2 second date to compare.
     */
    private void assertEqualsDates(final Date d1, final Date d2) {
        if (Math.abs(d1.getTime() - d2.getTime()) > 60001) {
            throw new AssertionFailedError("Not equals dates: " + d1 + " != " + d2);
        }
    }
}
