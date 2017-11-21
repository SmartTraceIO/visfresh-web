/**
 *
 */
package com.visfresh.services;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.mail.MessagingException;

import org.junit.Before;
import org.junit.Test;

import com.visfresh.entities.RestSession;
import com.visfresh.entities.User;
import com.visfresh.impl.services.DefaultRestSessionManager;
import com.visfresh.io.email.EmailMessage;

import junit.framework.AssertionFailedError;

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
    @Before
    public void setUp() {
        this.sessionManager = new DefaultRestSessionManager() {
            /* (non-Javadoc)
             * @see com.visfresh.services.DefaultRestSessionManager#deleteSession(com.visfresh.entities.RestSession)
             */
            @Override
            protected void deleteSession(final RestSession s) {
                deleteSessionImpl(s);
            }
            /* (non-Javadoc)
             * @see com.visfresh.services.DefaultRestSessionManager#findSessionByToken(java.lang.String)
             */
            @Override
            protected RestSession findSessionByToken(final String token) {
                return findSessionByTokenImpl(token);
            }
            /* (non-Javadoc)
             * @see com.visfresh.services.DefaultRestSessionManager#saveSession(com.visfresh.entities.RestSession)
             */
            @Override
            protected void saveSession(final RestSession s) {
                saveSessionImpl(s);
            }
        };
    }

    @Test
    public void testLogin() throws AuthenticationException {
        //create one user
        final User u = createUser();

        final AuthToken token = login(u.getEmail(), "password", "junit");
        assertNotNull(token);
        assertEquals(u.getId(), getUserForToken(token.getToken()).getId());
        assertNotNull(token.getClientInstanceId());

        assertEquals(1, sessions.size());
        assertEquals(u.getId(), sessions.values().iterator().next().getUser().getId());
    }
    @Test
    public void testLoginWithNullInstance() throws AuthenticationException {
        //create one user
        final User u = createUser();

        final AuthToken token = login(u.getEmail(), "password", null);

        //not null instance should be returned to user
        assertNotNull(token.getClientInstanceId());

        //but session should be stored by null instance.
        assertNull(sessions.values().iterator().next().getToken().getClientInstanceId());
    }
    @Test
    public void testAuthFailed() {
        //create one user
        final User u = createUser();

        try {
            login(u.getEmail(), "password1", "junit");
            throw new AssertionFailedError("Auth exception should be thrown");
        } catch (final AuthenticationException e) {
            //correct behavior;
        }

        assertEquals(0, sessions.size());
    }
    @Test
    public void testRefreshToken() throws AuthenticationException {
        //create one user
        final User u = createUser();
        final String token = login(u.getEmail(), "password", "junit").getToken();

        final AuthToken newToken = refreshToken(token);
        assertNotNull(sessionManager.getSession(newToken.getToken()));
        assertNull(sessionManager.getSession(token));
    }
    @Test
    public void testNotUser() {
        try {
            login("email@junit.ru", "password1", "junit");
            throw new AssertionFailedError("Auth exception should be thrown");
        } catch (final AuthenticationException e) {
            //correct behavior;
        }

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

    protected void deleteSessionImpl(final RestSession s) {
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
    protected RestSession findSessionByTokenImpl(final String token) {
        for (final RestSession s: sessions.values()) {
            if (s.getToken().getToken().equals(token)) {
                return s;
            }
        }
        return null;
    }
    protected RestSession saveSessionImpl(final RestSession s) {
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
}
