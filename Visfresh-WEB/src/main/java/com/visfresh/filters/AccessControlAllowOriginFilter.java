/**
 *
 */
package com.visfresh.filters;

import java.io.IOException;
import java.util.Enumeration;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class AccessControlAllowOriginFilter implements Filter {

    /**
     * Default constructor.
     */
    public AccessControlAllowOriginFilter() {
        super();
    }

    /* (non-Javadoc)
     * @see javax.servlet.Filter#init(javax.servlet.FilterConfig)
     */
    @Override
    public void init(final FilterConfig filterConfig) throws ServletException {
    }

    /* (non-Javadoc)
     * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest, javax.servlet.ServletResponse, javax.servlet.FilterChain)
     */
    @Override
    public void doFilter(final ServletRequest request, final ServletResponse response,
            final FilterChain chain) throws IOException, ServletException {
        doFilter((HttpServletRequest) request, (HttpServletResponse) response, chain);
    }
    /**
     * @param request servlet request.
     * @param response servlet response.
     * @param chain filter chain.
     * @throws IOException
     * @throws ServletException
     */
    private void doFilter(final HttpServletRequest request, final HttpServletResponse response,
            final FilterChain chain) throws IOException, ServletException {
        System.out.println("Context path: " + request.getContextPath());
        System.out.println("Path info: " + request.getPathInfo());
        final Enumeration<?> names = request.getHeaderNames();
        while (names.hasMoreElements()) {
            final String key = (String) names.nextElement();
            System.out.println(key + ": " + request.getHeader(key));
        }

        if (request.getHeader("origin") != null) {
            response.setHeader("Access-Control-Allow-Origin", "*");
        }
        chain.doFilter(request, response);
    }

    /* (non-Javadoc)
     * @see javax.servlet.Filter#destroy()
     */
    @Override
    public void destroy() {
    }
}
