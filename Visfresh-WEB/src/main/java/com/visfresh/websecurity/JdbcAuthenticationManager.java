/**
 *
 */
package com.visfresh.websecurity;

import java.util.HashSet;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.stereotype.Component;

import com.visfresh.entities.Role;
import com.visfresh.entities.User;
import com.visfresh.services.AuthService;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class JdbcAuthenticationManager implements AuthenticationManager {
    @Autowired
    private AuthService service;

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
        final String token = (String) authentication.getCredentials();
        final User auth = service.getUserForToken(token);
        if (auth == null) {
            throw new BadCredentialsException("User session for given token not found, possible expired");
        }

        final Set<GrantedAuthority> roleSet = new HashSet<>();
        for (final Role role : auth.getRoles()) {
            final GrantedAuthority ga = new SimpleGrantedAuthority("ROLE_" + role.name());
            roleSet.add(ga);
        }

        final org.springframework.security.core.userdetails.User user = new org.springframework.security.core.userdetails.User(
                auth.getEmail(), token, true, true, true, true, roleSet);
        return new PreAuthenticatedAuthenticationToken(user, auth, roleSet);
    }
}
