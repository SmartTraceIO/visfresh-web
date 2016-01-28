/**
 *
 */
package com.visfresh.utils;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class MessagesTest {

    /**
     * Default constructor.
     */
    public MessagesTest() {
        super();
    }

    @Test
    public void testResetPasswordMessages() {
        final Map<String, String> replacements = new HashMap<String, String>();
        final String url = "http://visfresh.com";
        final String email = "developer@visfresh.com";
        replacements.put("url", url);
        replacements.put("email", email);

        String msg = Messages.getMessage("passwordreset.reset.text", replacements);
        assertNotNull(msg);
        assertTrue(msg.contains(url));

        assertNotNull(Messages.getMessage("passwordreset.reset.subject", replacements));

        msg = Messages.getMessage("passwordreset.userNotFound", replacements);
        assertNotNull(msg);
        assertTrue(msg.contains(email));

        msg = Messages.getMessage("passwordreset.resetSuccesfully", replacements);
        assertNotNull(msg);

        msg = Messages.getMessage("passwordreset.reset.requestNotFound", replacements);
        assertNotNull(msg);
        assertTrue(msg.contains(email));

        //check token matches
        msg = Messages.getMessage("passwordreset.reset.tokenNotMatches", replacements);
        assertNotNull(msg);

        msg = Messages.getMessage("passwordreset.reset.successfully", replacements);
        assertNotNull(msg);
    }
}
