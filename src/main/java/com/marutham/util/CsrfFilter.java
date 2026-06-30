package com.marutham.util;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.security.SecureRandom;
import java.util.Base64;


@WebFilter("/*")
public class CsrfFilter implements Filter {
    private static final Logger logger = LoggerFactory.getLogger(CsrfFilter.class);
    private static final String CSRF_TOKEN_SESSION_KEY = "_csrf_token";
    private static final String CSRF_TOKEN_HEADER_KEY = "X-CSRF-Token";
    private static final String CSRF_TOKEN_PARAM_KEY = "_csrf";
    private static final int TOKEN_LENGTH = 32;
    private static final SecureRandom random = new SecureRandom();


    private static final String[] PROTECTED_METHODS = {"POST", "PUT", "DELETE", "PATCH"};

    private static final String[] EXEMPT_PATHS = {
            "/login", "/register", "/logout", 
            "/css/", "/js/", "/images/", "/favicon.ico"
    };

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {}

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) 
            throws IOException, ServletException {
        
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;
        HttpSession session = req.getSession(false);

        String uri = req.getRequestURI();
        String method = req.getMethod();

        if (session == null && isProtectedPath(uri)) {
            session = req.getSession(true);
        }

        String csrfToken = null;
        if (session != null) {
            csrfToken = (String) session.getAttribute(CSRF_TOKEN_SESSION_KEY);
            if (csrfToken == null) {
                csrfToken = generateToken();
                session.setAttribute(CSRF_TOKEN_SESSION_KEY, csrfToken);
            }
        }

        if ("GET".equalsIgnoreCase(method)) {
            if (csrfToken != null) {
                res.addHeader(CSRF_TOKEN_HEADER_KEY, csrfToken);
            }
            chain.doFilter(request, response);
            return;
        }

        boolean needsCsrfProtection = false;
        for (String protectedMethod : PROTECTED_METHODS) {
            if (protectedMethod.equalsIgnoreCase(method)) {
                needsCsrfProtection = true;
                break;
            }
        }

        if (needsCsrfProtection) {
            for (String exemptPath : EXEMPT_PATHS) {
                if (uri.contains(exemptPath)) {
                    needsCsrfProtection = false;
                    break;
                }
            }
        }

        if (needsCsrfProtection) {
            if (session == null || csrfToken == null) {
                logger.warn("CSRF validation failed: No session or token for {}", uri);
                res.setStatus(HttpServletResponse.SC_FORBIDDEN);
                res.setContentType("application/json");
                res.getWriter().write("{\"success\":false,\"message\":\"CSRF validation failed: Session expired\"}");
                return;
            }

            String tokenFromRequest = req.getHeader(CSRF_TOKEN_HEADER_KEY);
            if (tokenFromRequest == null || tokenFromRequest.isEmpty()) {
                tokenFromRequest = req.getParameter(CSRF_TOKEN_PARAM_KEY);
            }

            if (tokenFromRequest == null || !tokenFromRequest.equals(csrfToken)) {
                logger.warn("CSRF token mismatch for {} from {}", uri, req.getRemoteAddr());
                ErrorHandler.logSecurityEvent("CSRF_VIOLATION", 
                    String.valueOf(session.getAttribute("userId")), 
                    "Token mismatch on " + uri);
                res.setStatus(HttpServletResponse.SC_FORBIDDEN);
                res.setContentType("application/json");
                res.getWriter().write("{\"success\":false,\"message\":\"CSRF validation failed: Invalid token\"}");
                return;
            }
        }

        chain.doFilter(request, response);
    }


    private boolean isProtectedPath(String uri) {
        return !uri.contains(".") && 
               !uri.contains("/login") && 
               !uri.contains("/register") &&
               !uri.contains("/logout") &&
               !uri.contains("/favicon");
    }


    private String generateToken() {
        byte[] randomBytes = new byte[TOKEN_LENGTH];
        random.nextBytes(randomBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }

    @Override
    public void destroy() {}
}
