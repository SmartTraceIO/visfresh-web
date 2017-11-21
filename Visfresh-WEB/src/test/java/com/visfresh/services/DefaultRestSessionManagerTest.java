/**
 *
 */
package com.visfresh.services;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.junit.Test;

import com.visfresh.entities.RestSession;
import com.visfresh.entities.User;
import com.visfresh.impl.services.DefaultRestSessionManager;
import com.visfresh.utils.HashGenerator;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class DefaultRestSessionManagerTest extends DefaultRestSessionManager {
    private long lastId = 10000l;
    private final Map<Long, RestSession> sessions = new HashMap<>();
    private final Random random = new Random();

    /**
     * Default constructor.
     */
    public DefaultRestSessionManagerTest() {
        super();
    }

    @Test
    public void testRestoreSessionsOnInitialize() {
        //create one user
        final User u1 = createUser();

        final RestSession s1 = new RestSession();
        s1.setUser(u1);
        s1.setToken(generateNewToken(u1, null));

        saveSession(s1);

        //create second user
        final User u2 = createUser();

        final RestSession s2 = new RestSession();
        s2.setUser(u2);
        s2.setToken(generateNewToken(u2, null));

        saveSession(s2);

        //initialize
        init();

        //check sessions loaded
        assertEquals(u1.getId(), getSession(s1.getToken().getToken()).getUser().getId());
        assertEquals(u2.getId(), getSession(s2.getToken().getToken()).getUser().getId());
    }
    @Test
    public void testSessionExpired() throws AuthenticationException {
        //create one user
        final User u = createUser();
        final AuthToken token = generateNewToken(u, null);
        createSession(u, token);

        token.setExpirationTime(new Date(System.currentTimeMillis() - 100000000l));

        this.removeExpiredTokens();

        assertNull(getSession(token.getToken()));
        //test REST session has removed from DB.
        assertEquals(0, sessions.size());
    }
    @Test
    public void testRemoveRedundantSessions() {
        final User u = createUser();

        createSession(u, generateNewToken(u, "a"));
        createSession(u, generateNewToken(u, "b"));
        createSession(u, generateNewToken(u, "b"));

        assertEquals(2, sessions.size());

        createSession(u, generateNewToken(u, null));
        assertEquals(3, sessions.size());

        createSession(u, generateNewToken(u, null));
        assertEquals(3, sessions.size());

    }
    /**
     * @return
     */
    private User createUser() {
        final User u = new User();
        u.setId(lastId++);
        u.setActive(true);
        u.setFirstName("FirstName");
        u.setLastName("LastName");
        u.setPassword(HashGenerator.createMd5Hash("password"));
        u.setEmail(u.getId() + "@junit.ru");
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
    protected void saveSession(final RestSession s) {
        if (s.getId() == null) {
            s.setId(lastId++);
        }
        sessions.put(s.getId(), s);
    }
    /* (non-Javadoc)
     * @see com.visfresh.services.DefaultAuthService#loadSessions()
     */
    @Override
    protected List<RestSession> loadSessions() {
        return new LinkedList<>(sessions.values());
    }

    /**
     * @param user user.
     * @return
     */
    private AuthToken generateNewToken(final User user, final String clientId) {
        final String token = user.getId() + "-" + HashGenerator.createMd5Hash(Long.toString(random.nextLong()));
        final AuthToken t = new AuthToken(token);
        t.setExpirationTime(new Date(System.currentTimeMillis() + 14400000l));
        t.setClientInstanceId(clientId);
        return t;
    }
}
