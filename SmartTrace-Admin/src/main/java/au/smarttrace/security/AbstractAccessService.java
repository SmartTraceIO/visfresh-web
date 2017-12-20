/**
 *
 */
package au.smarttrace.security;

import java.util.Date;
import java.util.Random;

import au.smarttrace.User;
import au.smarttrace.utils.HashGenerator;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public abstract class AbstractAccessService implements AccessService {
    /**
     * Access token prefix.
     */
    public static final String TOKEN_PREFIX = "tkn-";
    private long sessionTimeOut = 15 * 60 * 1000l;
    private static final Random random = new Random();

    /* (non-Javadoc)
     * @see demo.music.auth.AccessService#login(java.lang.String, java.lang.String)
     */
    @Override
    public AuthInfo login(final String email, final String password) throws AccessException {
        final User user = findUserByEmailPassword(email, password);
        if (user == null) {
            throw new AccessException("Bad credentials");
        }

        final AccessToken token = createToken();
        saveNewUserSession(user.getId(), token);

        //create result info.
        final AuthInfo info = new AuthInfo();
        info.setUser(user);
        info.setToken(token);
        return info;
    }
    /**
     * @return
     */
    private AccessToken createToken() {
        final AccessToken t = new AccessToken(TOKEN_PREFIX
                + HashGenerator.generateHash(Long.toString(random.nextLong())));
        t.setExpirationTime(new Date(System.currentTimeMillis() + sessionTimeOut));
        return t ;
    }
    /* (non-Javadoc)
     * @see demo.music.auth.AccessService#logout(java.lang.String)
     */
    @Override
    public void logout(final String accessToken) {
        removeUserSession(accessToken);
    }

    /* (non-Javadoc)
     * @see demo.music.auth.AccessService#refreshToken(java.lang.String)
     */
    @Override
    public AccessToken refreshToken(final String accessToken) throws AccessException {
        final AuthInfo info = getAuthForToken(accessToken);
        if (info == null) {
            throw new AccessException("Session expired");
        }

        final AccessToken token = createToken();
        removeUserSession(token.getToken());
        saveNewUserSession(info.getUser().getId(), token);
        return token;
    }
    /**
     * @param email user's email.
     * @param passwordHash encrypted user password. Database stores only encrypted passwords.
     * @return user.
     */
    protected abstract User findUserByEmailPassword(String email, String passwordHash);
    /**
     * @param userId email address.
     * @param token access token.
     */
    protected abstract void saveNewUserSession(Long userId, AccessToken token);
    /**
     * @param accessToken access token for session to remove
     */
    protected abstract void removeUserSession(String accessToken);
}
