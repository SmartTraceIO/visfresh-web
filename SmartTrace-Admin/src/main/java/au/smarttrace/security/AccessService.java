/**
 *
 */
package au.smarttrace.security;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public interface AccessService {
    AuthInfo login(String email, String password) throws AccessException;
    void logout(String accessToken);
    AccessToken refreshToken(String accessToken) throws AccessException;
    AuthInfo getAuthForToken(String accessToken);
}
