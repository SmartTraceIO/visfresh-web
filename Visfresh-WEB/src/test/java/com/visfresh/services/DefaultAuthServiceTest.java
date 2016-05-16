/**
 *
 */
package com.visfresh.services;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.mail.MessagingException;

import junit.framework.AssertionFailedError;

import org.junit.Test;

import com.visfresh.entities.RestSession;
import com.visfresh.entities.User;
import com.visfresh.io.email.EmailMessage;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class DefaultAuthServiceTest extends DefaultAuthService {
    private final List<EmailMessage> emails = new LinkedList<>();
    private long lastId = 10000l;
    private final Map<Long, RestSession> sessions = new HashMap<>();
    private final Map<Long, User> users = new HashMap<>();

    /**
     * Default constructor.
     */
    public DefaultAuthServiceTest() {
        super();
    }

    @Test
    public void testRestoreSessionsOnInitialize() {
        //create one user
        final User u1 = createUser();

        final RestSession s1 = new RestSession();
        s1.setUser(u1);
        s1.setToken(generateNewToken(u1));

        saveSession(s1);

        //create second user
        final User u2 = createUser();

        final RestSession s2 = new RestSession();
        s2.setUser(u2);
        s2.setToken(generateNewToken(u2));

        saveSession(s2);

        //initialize
        init();

        //check sessions loaded
        assertEquals(u1.getId(), getUserForToken(s1.getToken().getToken()).getId());
        assertEquals(u2.getId(), getUserForToken(s2.getToken().getToken()).getId());
    }

    @Test
    public void testLogin() throws AuthenticationException {
        //create one user
        final User u = createUser();

        final AuthToken token = login(u.getEmail(), "password");
        assertNotNull(token);
        assertEquals(u.getId(), getUserForToken(token.getToken()).getId());

        assertEquals(1, sessions.size());
        assertEquals(u.getId(), sessions.values().iterator().next().getUser().getId());
    }
    @Test
    public void testAuthFailed() {
        //create one user
        final User u = createUser();

        try {
            login(u.getEmail(), "password1");
            throw new AssertionFailedError("Auth exception should be thrown");
        } catch (final AuthenticationException e) {
            //correct behavior;
        }

        assertEquals(0, sessions.size());
    }
    @Test
    public void testNotUser() {
        try {
            login("email@junit.ru", "password1");
            throw new AssertionFailedError("Auth exception should be thrown");
        } catch (final AuthenticationException e) {
            //correct behavior;
        }

        assertEquals(0, sessions.size());
    }
    @Test
    public void testSessionExcpired() throws AuthenticationException {
        //create one user
        final User u = createUser();
        final AuthToken token = login(u.getEmail(), "password");
        token.setExpirationTime(new Date(System.currentTimeMillis() - 100000000l));

        this.removeExpiredTokens();

        assertNull(getUserForToken(token.getToken()));
        //test REST session has removed from DB.
        assertEquals(0, sessions.size());
    }
    /**
     * @return
     */
    private User createUser() {
        final User u = new User();
        u.setActive(true);
        u.setFirstName("FirstName");
        u.setLastName("LastName");
        u.setPassword(generateHash("password"));

        saveUser(u);
        u.setEmail(u.getId() + "@.junit.ru");
        return u;
    }

    /* (non-Javadoc)
     * @see com.visfresh.services.DefaultAuthService#deleteSession(com.visfresh.entities.RestSession)
     */
    @Override
    protected void deleteSession(final RestSession s) {
        sessions.remove(s.getId());
    }
    /* (non-Javadoc)
     * @see com.visfresh.services.DefaultAuthService#findByEmail(java.lang.String)
     */
    @Override
    protected User findUserByEmail(final String email) {
        for (final User u: users.values()) {
            if (u.getEmail().equals(email)) {
                return u;
            }
        }
        return null;
    }
    /* (non-Javadoc)
     * @see com.visfresh.services.DefaultAuthService#findSessionByToken(java.lang.String)
     */
    @Override
    protected RestSession findSessionByToken(final String token) {
        for (final RestSession s: sessions.values()) {
            if (s.getToken().getToken().equals(token)) {
                return s;
            }
        }
        return null;
    }
    /* (non-Javadoc)
     * @see com.visfresh.services.DefaultAuthService#saveSession(com.visfresh.entities.RestSession)
     */
    @Override
    protected RestSession saveSession(final RestSession s) {
        if (s.getId() == null) {
            s.setId(lastId++);
        }
        sessions.put(s.getId(), s);
        return s;
    }
    /* (non-Javadoc)
     * @see com.visfresh.services.DefaultAuthService#saveUserToDb(com.visfresh.entities.User)
     */
    @Override
    protected User saveUser(final User user) {
        if (user.getId() == null) {
            user.setId(lastId++);
        }
        return users.put(user.getId(), user);
    }
    /* (non-Javadoc)
     * @see com.visfresh.services.DefaultAuthService#sendEmail(java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    protected void sendEmail(final String email, final String subject, final String message)
            throws MessagingException {
        final EmailMessage e = new EmailMessage();
        e.setEmails(new String[] {email});
        e.setSubject(subject);
        e.setMessage(message);
        emails.add(e);
    }
    /* (non-Javadoc)
     * @see com.visfresh.services.DefaultAuthService#loadSessions()
     */
    @Override
    protected List<RestSession> loadSessions() {
        return new LinkedList<>(sessions.values());
    }
}
