/**
 *
 */
package au.smarttrace.security;

import java.util.HashSet;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.stereotype.Component;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class JdbcAuthenticationManager implements AuthenticationManager {
    @Autowired
    private AccessService service;

    /**
     * Default constructor.
     */
    public JdbcAuthenticationManager() {
        super();
    }

    /* (non-Javadoc)
     * @see org.springframework.security.authentication.AuthenticationManager#authenticate(org.springframework.security.core.Authentication)
     */
    @Override
    public Authentication authenticate(final Authentication authentication) throws AuthenticationException {
        final AuthInfo auth = service.getAuthForToken((String) authentication.getCredentials());
        if (auth == null) {
            throw new BadCredentialsException("User session for given token not found, possible expired");
        }

        final Set<GrantedAuthority> roleSet = new HashSet<>();
        for (final String role : auth.getUser().getRoles()) {
            final GrantedAuthority ga = new SimpleGrantedAuthority("ROLE_" + role);
            roleSet.add(ga);
        }

        final User user = new User(auth.getUser().getEmail(), auth.getToken().getToken(),
                true, true, true, true, roleSet);
        return new PreAuthenticatedAuthenticationToken(user, auth, roleSet);
    }
}
