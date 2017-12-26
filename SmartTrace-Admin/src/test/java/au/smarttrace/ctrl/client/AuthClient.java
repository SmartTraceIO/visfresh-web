/**
 *
 */
package au.smarttrace.ctrl.client;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import au.smarttrace.ctrl.client.resp.AnyResponse;
import au.smarttrace.ctrl.client.resp.AuthInfoResponse;
import au.smarttrace.ctrl.client.resp.TokenResponse;
import au.smarttrace.security.AccessToken;
import au.smarttrace.security.AuthInfo;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class AuthClient extends BaseClient {
    /**
     * Default constructor.
     */
    public AuthClient() {
        super();
    }

    /**
     * @param email email address.
     * @param password password.
     * @return authentication token.
     * @throws IOException
     * @throws AuthenticationException
     */
    public final AuthInfo login(final String email, final String password) throws ServiceException, IOException {
        final Map<String, String> req = new HashMap<String, String>();
        req.put("email", email);
        req.put("password", password);

        return sendGetRequest("login", req, AuthInfoResponse.class);
    }

    /**
     * @return authentication information for currently logged in user.
     * @throws ServiceException
     * @throws IOException
     */
    public AuthInfo getAuthInfo() throws IOException, ServiceException {
        return sendGetRequest(getPathWithToken("getAuthInfo"), new HashMap<String, String>(), AuthInfoResponse.class);
    }
    /**
     * @param authToken
     * @throws IOException
     */
    public final void logout() throws ServiceException, IOException {
        final Map<String, String> params = new HashMap<String, String>();
        sendGetRequest(getPathWithToken("logout"), params, AnyResponse.class);
    }
    /**
     * @return
     * @throws ServiceException
     * @throws IOException
     */
    public final AccessToken refreshToken() throws IOException, ServiceException {
        return sendGetRequest(getPathWithToken("refreshToken"), new HashMap<String, String>(), TokenResponse.class);
    }
}
