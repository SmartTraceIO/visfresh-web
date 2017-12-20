/**
 *
 */
package au.smarttrace.svc;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import au.smarttrace.User;
import au.smarttrace.security.AbstractAccessService;
import au.smarttrace.security.AccessToken;
import au.smarttrace.security.AuthInfo;
import au.smarttrace.security.ClientSessionsDao;
import au.smarttrace.user.UsersService;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class AccessServiceImpl extends AbstractAccessService {
    @Autowired
    private UsersService userService;
    @Autowired
    private ClientSessionsDao clientSessionsDao;

    /**
     * Default constructor.
     */
    public AccessServiceImpl() {
        super();
    }

    /* (non-Javadoc)
     * @see demo.music.security.AbstractAccessService#findUserByEmailPassword(java.lang.String, java.lang.String)
     */
    @Override
    protected User findUserByEmailPassword(final String email, final String password) {
        return userService.findUserByEmailPassword(email, password);
    }

    /* (non-Javadoc)
     * @see demo.music.auth.AccessService#getAuthForToken(java.lang.String)
     */
    @Override
    public AuthInfo getAuthForToken(final String accessToken) {
        return clientSessionsDao.getAuthInfo(accessToken);
    }
    /* (non-Javadoc)
     * @see demo.music.auth.AbstractAccessService#saveNewUserSession(java.lang.String, demo.music.auth.AccessToken)
     */
    @Override
    protected void saveNewUserSession(final Long userId, final AccessToken token) {
        clientSessionsDao.createSession(userId, token);
    }
    /* (non-Javadoc)
     * @see demo.music.auth.AbstractAccessService#removeUserSession(java.lang.String)
     */
    @Override
    protected void removeUserSession(final String accessToken) {
        clientSessionsDao.deleteSession(accessToken);
    }
}
