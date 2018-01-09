/**
 *
 */
package com.visfresh.websecurity;

import java.io.IOException;
import java.util.HashSet;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.stereotype.Component;

import com.visfresh.controllers.io.GlobalDefaultExceptionHandler;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
@Component
public class AuthTokenAuthenticationFilter extends AbstractAuthenticationProcessingFilter {
    @Autowired
    private AuthenticationManager authManager;

    /**
     * @param defaultFilterProcessesUrl
     */
    public AuthTokenAuthenticationFilter() {
        super("/**");
    }

    /* (non-Javadoc)
     * @see org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter#unsuccessfulAuthentication(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, org.springframework.security.core.AuthenticationException)
     */
    @Override
    protected void unsuccessfulAuthentication(final HttpServletRequest request, final HttpServletResponse response,
            final AuthenticationException failed) throws IOException, ServletException {
        GlobalDefaultExceptionHandler.handleError(response, failed);
    }
    /* (non-Javadoc)
     * @see org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter#successfulAuthentication(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, javax.servlet.FilterChain, org.springframework.security.core.Authentication)
     */
    @Override
    protected void successfulAuthentication(final HttpServletRequest request, final HttpServletResponse response, final FilterChain chain,
            final Authentication authResult) throws IOException, ServletException {
        SecurityContextHolder.getContext().setAuthentication(authResult);

        if (authResult.isAuthenticated()) {
            final String path = getPath(request);
            final String token = getToken(request);
            final int offset = path.indexOf(token);

            final String newPath = path.substring(0, offset - 1) + path.substring(offset + token.length());
            request.getRequestDispatcher(newPath).forward(request, response);
        } else {
            chain.doFilter(request, response);
        }
    }
    /*
     * (non-Javadoc)
     *
     * @see org.springframework.security.web.authentication.
     * AbstractAuthenticationProcessingFilter#attemptAuthentication(javax.
     * servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    @Override
    public Authentication attemptAuthentication(final HttpServletRequest request, final HttpServletResponse response)
            throws AuthenticationException, IOException, ServletException {
        //redirect to URL without token
        final String token = getToken(request);

        if (token == null) {
            return new TokenAuthentication(new HashSet<>(), null);
        }

        try {
            return getAuthenticationManager().authenticate(new UsernamePasswordAuthenticationToken(null, token));
        } catch (final Exception e) {
            GlobalDefaultExceptionHandler.handleError(response, e);
            return null;
        }
    }
    /**
     * @param request
     * @return
     */
    protected String getToken(final HttpServletRequest request) {
        final String path = request.getPathInfo();
        final int index = path.lastIndexOf('/') + 1;

        String token = null;
        if (index > -1) {
            token = path.substring(index, path.indexOf('/', index + 1));
        }
        return token;
    }
    /* (non-Javadoc)
     * @see org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter#afterPropertiesSet()
     */
    @Override
    public void afterPropertiesSet() {
        setAuthenticationManager(authManager);
        super.afterPropertiesSet();
    }
    /**
     * @param request
     *            servlet request.
     * @return request path.
     */
    private String getPath(final HttpServletRequest request) {
        return request.getRequestURI().substring(request.getContextPath().length());
    }
}
