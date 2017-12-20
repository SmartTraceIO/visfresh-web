/**
 *
 */
package au.smarttrace.security;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public interface ClientSessionsDao {
    /**
     * @param token session token.
     */
    void deleteSession(String token);
    /**
     * @param token
     * @return auth info for given user.
     */
    AuthInfo getAuthInfo(String token);
    /**
     * @param userId user's email.
     * @param token access token.
     */
    void createSession(Long userId, AccessToken token);
}
