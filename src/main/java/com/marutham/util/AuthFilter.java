package com.marutham.util;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

@WebFilter("/*")
public class AuthFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {}

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;
        HttpSession session = req.getSession(false);
        String contextPath = req.getContextPath();
        String uri = req.getRequestURI();
        String path = uri.substring(contextPath.length());

        if (path == null || path.isEmpty() || path.equals("/")) {
            chain.doFilter(request, response);
            return;
        }

        if (path.equals("/favicon.ico")) {
            try {
                if (req.getServletContext().getResource("/favicon.ico") == null) {
                    res.setStatus(HttpServletResponse.SC_NO_CONTENT);
                    return;
                }
            } catch (Exception e) {
                res.setStatus(HttpServletResponse.SC_NO_CONTENT);
                return;
            }
        }

        if (path.equals("/status")) {
            chain.doFilter(request, response);
            return;
        }

        if (path.contains(".") && (path.endsWith(".css") || path.endsWith(".js") || path.endsWith(".png") ||
            path.endsWith(".jpg") || path.endsWith(".svg") || path.endsWith(".ico") || path.endsWith(".html"))) {

            if (path.endsWith("dashboard.html") || path.endsWith("forum.html") || path.endsWith("admin.html") ||
                path.endsWith("prices.html") || path.endsWith("equipment.html") || path.endsWith("consultation.html") ||
                path.endsWith("recommendation.html") ||
                path.endsWith("profile.html") || path.endsWith("experts.html") || path.endsWith("calendar.html")) {

                if (session == null || session.getAttribute("user") == null) {
                    res.sendRedirect(contextPath + "/login.html");
                    return;
                }
            }
            chain.doFilter(request, response);
            return;
        }

        if (path.equals("/login") || path.equals("/register") || path.equals("/logout")) {
            chain.doFilter(request, response);
            return;
        }

        boolean loggedIn = (session != null && session.getAttribute("user") != null);
        if (loggedIn) {
            chain.doFilter(request, response);
        } else {
            res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            res.setContentType("application/json");
            res.getWriter().write("{\"success\":false,\"message\":\"Unauthorized\"}");
        }
    }

    @Override
    public void destroy() {}
}