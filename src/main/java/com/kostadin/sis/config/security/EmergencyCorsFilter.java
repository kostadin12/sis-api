package com.kostadin.sis.config.security;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Emergency CORS filter that handles any CORS requests with the highest priority.
 * This filter will execute before any other filters including security filters.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class EmergencyCorsFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // Nothing to do
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;

        // Get the origin from the request
        String origin = request.getHeader("Origin");

        // If origin is null, allow all origins, otherwise set the specific origin
        if (origin != null) {
            response.setHeader("Access-Control-Allow-Origin", origin);
        } else {
            response.setHeader("Access-Control-Allow-Origin", "*");
        }

        // Configure other CORS headers
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, PATCH, HEAD");
        response.setHeader("Access-Control-Max-Age", "3600");
        response.setHeader("Access-Control-Allow-Headers", "Origin, X-Requested-With, Content-Type, Accept, Authorization, X-Auth-Token");
        response.setHeader("Access-Control-Expose-Headers", "Authorization, X-Auth-Token");
        response.setHeader("Access-Control-Allow-Credentials", "true");

        // Handle OPTIONS preflight requests
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            response.setStatus(HttpServletResponse.SC_OK);
        } else {
            // Continue the filter chain for non-OPTIONS requests
            chain.doFilter(req, res);
        }
    }

    @Override
    public void destroy() {
        // Nothing to do
    }
}