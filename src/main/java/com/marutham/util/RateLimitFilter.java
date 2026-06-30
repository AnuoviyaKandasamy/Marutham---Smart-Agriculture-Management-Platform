package com.marutham.util;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;


@WebFilter("/*")
public class RateLimitFilter implements Filter {
    private static final Logger logger = LoggerFactory.getLogger(RateLimitFilter.class);

    private static final long MINUTE = 60 * 1000L;
    private static final long HOUR   = 60 * MINUTE;
    private static final long DAY    = 24 * HOUR;
    private static final int  SC_TOO_MANY_REQUESTS = 429;

    private static final Map<String, RateLimitConfig> RATE_LIMITS = new HashMap<>();
    static {
        RATE_LIMITS.put("/posts",             new RateLimitConfig(10, HOUR, "10 posts per hour"));
        RATE_LIMITS.put("/consultation",      new RateLimitConfig(5,  HOUR, "5 questions per hour"));
        RATE_LIMITS.put("/equipment-requests",new RateLimitConfig(20, DAY,  "20 requests per day"));
    }

    private static final Map<String, Map<String, LinkedList<Long>>> requestCounts = new ConcurrentHashMap<>();


    private ScheduledExecutorService cleanupScheduler;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        cleanupScheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "rate-limit-cleanup");
            t.setDaemon(true);
            return t;
        });
        cleanupScheduler.scheduleAtFixedRate(RateLimitFilter::cleanupOldEntries, 1, 1, TimeUnit.HOURS);
        logger.info("RateLimitFilter initialized with scheduled cleanup.");
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest  req = (HttpServletRequest)  request;
        HttpServletResponse res = (HttpServletResponse) response;
        HttpSession session = req.getSession(false);

        if (session == null || session.getAttribute("userId") == null) {
            chain.doFilter(request, response);
            return;
        }

        int userId    = (int) session.getAttribute("userId");
        String uri    = req.getRequestURI();
        String method = req.getMethod();

        if (!("POST".equals(method) || "PUT".equals(method) || "DELETE".equals(method))) {
            chain.doFilter(request, response);
            return;
        }

        RateLimitConfig config = findMatchingRateLimit(uri);
        if (config != null) {
            if (!isAllowed(userId, uri, config)) {
                logger.warn("Rate limit exceeded for user {} on endpoint {}", userId, uri);
                ErrorHandler.logSecurityEvent("RATE_LIMIT_EXCEEDED", String.valueOf(userId),
                    uri + " - " + config.description);
                res.setStatus(SC_TOO_MANY_REQUESTS);
                res.setHeader("Retry-After", "60");
                res.setContentType("application/json");
                res.getWriter().write("{\"success\":false,\"message\":\"Rate limit exceeded: " + config.description + "\"}");
                return;
            }
            recordRequest(userId, uri);
        }
        chain.doFilter(request, response);
    }

    private RateLimitConfig findMatchingRateLimit(String uri) {
        for (Map.Entry<String, RateLimitConfig> e : RATE_LIMITS.entrySet()) {
            if (uri.contains(e.getKey())) return e.getValue();
        }
        return null;
    }

    private boolean isAllowed(int userId, String endpoint, RateLimitConfig config) {
        String userKey = "user_" + userId;
        long now = System.currentTimeMillis();
        Map<String, LinkedList<Long>> userRequests = requestCounts.computeIfAbsent(userKey, k -> new ConcurrentHashMap<>());
        LinkedList<Long> timestamps = userRequests.computeIfAbsent(endpoint, k -> new LinkedList<>());
        synchronized (timestamps) {
            timestamps.removeIf(ts -> now - ts > config.windowMs);
            return timestamps.size() < config.maxRequests;
        }
    }

    private void recordRequest(int userId, String endpoint) {
        String userKey = "user_" + userId;
        long now = System.currentTimeMillis();
        Map<String, LinkedList<Long>> userRequests = requestCounts.get(userKey);
        if (userRequests != null) {
            LinkedList<Long> timestamps = userRequests.get(endpoint);
            if (timestamps != null) {
                synchronized (timestamps) { timestamps.add(now); }
            }
        }
    }

    public static void cleanupOldEntries() {
        long now = System.currentTimeMillis();
        requestCounts.forEach((userId, endpoints) ->
            endpoints.forEach((endpoint, timestamps) -> {
                synchronized (timestamps) {
                    timestamps.removeIf(ts -> now - ts > DAY);
                }
            })
        );

        requestCounts.entrySet().removeIf(e -> e.getValue().values().stream().allMatch(List::isEmpty));
        logger.debug("Rate limit cleanup completed.");
    }

    @Override
    public void destroy() {
        if (cleanupScheduler != null) cleanupScheduler.shutdownNow();
    }

    private static class RateLimitConfig {
        int maxRequests; long windowMs; String description;
        RateLimitConfig(int m, long w, String d) { maxRequests = m; windowMs = w; description = d; }
    }
}
