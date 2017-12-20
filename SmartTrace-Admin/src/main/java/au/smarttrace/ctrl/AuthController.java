/**
 *
 */
package au.smarttrace.ctrl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import au.smarttrace.Roles;
import au.smarttrace.security.AccessException;
import au.smarttrace.security.AccessService;
import au.smarttrace.security.AccessToken;
import au.smarttrace.security.AuthInfo;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@RestController("auth")
@RequestMapping(produces = "application/json;charset=UTF-8")
public class AuthController {
    private final Logger log = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private AccessService service;

    /**
     * Default constructor.
     */
    public AuthController() {
        super();
    }

    /**
     * @param email email.
     * @param password password.
     * @return authorization information for currently logged in user in case of successfully.
     * @throws AccessException in case of unsuccessfully authentication.
     */
    @RequestMapping(value = "/login", method = RequestMethod.GET)
    public AuthInfo login(final @RequestParam String email, final @RequestParam String password)
            throws AccessException {
        final AuthInfo login = service.login(email, password);
        log.debug("Session for user " + login.getUser().getEmail() + " has created. Token " + login.getToken().getToken());
        return login;
    }
    /**
     * @return authentication information for logged in user.
     * @throws AccessException
     */
    @RequestMapping(value = "/getAuthInfo", method = RequestMethod.GET)
    @Secured({"ROLE_" + Roles.BasicUser, "ROLE_" + Roles.Admin,
        "ROLE_" + Roles.NormalUser, "ROLE_" + Roles.SmartTraceAdmin})
    public AuthInfo getAuthInfo() throws AccessException {
        final AuthInfo auth = getAuth();
        log.debug("User " + auth.getUser().getEmail() + " has request user info for session "
                + auth.getToken().getToken());
        return auth;
    }
    /**
     * Closes current user session.
     */
    @RequestMapping(value = "/logout", method = RequestMethod.GET)
    @Secured({"ROLE_" + Roles.BasicUser, "ROLE_" + Roles.Admin,
        "ROLE_" + Roles.NormalUser, "ROLE_" + Roles.SmartTraceAdmin})
    public String logout() {
        final AuthInfo auth = getAuth();
        service.logout(auth.getToken().getToken());
        log.debug("User " + auth.getUser().getEmail() + " has logout from session "
                + auth.getToken().getToken());
        return "OK";
    }
    /**
     * @param token old access token.
     * @return refreshed access token.
     * @throws AccessException in case of session losing or user not logged in.
     */
    @RequestMapping(value = "/refreshToken", method = RequestMethod.GET)
    @Secured({"ROLE_" + Roles.BasicUser, "ROLE_" + Roles.Admin,
        "ROLE_" + Roles.NormalUser, "ROLE_" + Roles.SmartTraceAdmin})
    public AccessToken refreshToken() throws AccessException {
        final AccessToken t = service.refreshToken(getAuth().getToken().getToken());
        final AuthInfo info = getAuth();
        log.debug("User " + info.getUser().getEmail() + " has refreshed session token to " + t.getToken());
        return t;
    }
    /**
     * @return supplied authentication information.
     */
    public static AuthInfo getAuth() {
        final Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getCredentials() != null) {
            return (AuthInfo) auth.getCredentials();
        }
        return null;
    }
}
