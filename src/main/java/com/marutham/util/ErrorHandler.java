package com.marutham.util;

import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;


public class ErrorHandler {
    private static final Logger logger = LoggerFactory.getLogger(ErrorHandler.class);

    public static void handleDatabaseError(SQLException e, HttpServletResponse response, String context) throws IOException {
        logger.error("Database error in {}: {}", context, e.getMessage(), e);
        
        String errorMessage = "Database error occurred";
        int statusCode = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;

        if (e.getMessage().contains("unique constraint") || e.getMessage().contains("Duplicate")) {
            errorMessage = "This record already exists";
            statusCode = HttpServletResponse.SC_CONFLICT;
        } else if (e.getMessage().contains("foreign key")) {
            errorMessage = "Cannot delete: referenced by other records";
            statusCode = HttpServletResponse.SC_BAD_REQUEST;
        }

        sendErrorJson(response, statusCode, errorMessage);
    }


    public static void handleValidationError(HttpServletResponse response, String fieldName, String message) throws IOException {
        logger.warn("Validation error for field '{}': {}", fieldName, message);
        
        Map<String, Object> error = new HashMap<>();
        error.put("success", false);
        error.put("message", message);
        error.put("field", fieldName);
        
        sendErrorJson(response, HttpServletResponse.SC_BAD_REQUEST, error);
    }

    public static void handleUnauthorized(HttpServletResponse response, String reason) throws IOException {
        logger.warn("Unauthorized access attempt: {}", reason);
        sendErrorJson(response, HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized: " + reason);
    }

    public static void handleForbidden(HttpServletResponse response, String reason) throws IOException {
        logger.warn("Forbidden access: {}", reason);
        sendErrorJson(response, HttpServletResponse.SC_FORBIDDEN, "Forbidden: " + reason);
    }

    public static void handleNotFound(HttpServletResponse response, String resource) throws IOException {
        logger.warn("Resource not found: {}", resource);
        sendErrorJson(response, HttpServletResponse.SC_NOT_FOUND, resource + " not found");
    }


    public static void sendErrorJson(HttpServletResponse response, int statusCode, String message) throws IOException {
        Map<String, Object> error = new HashMap<>();
        error.put("success", false);
        error.put("message", message);
        sendErrorJson(response, statusCode, error);
    }


    public static void sendErrorJson(HttpServletResponse response, int statusCode, Object errorData) throws IOException {
        response.setStatus(statusCode);
        response.setContentType("application/json");
        
        if (errorData instanceof Map) {
            String json = JsonUtil.toJson(errorData);
            response.getWriter().write(json);
        } else {
            String json = JsonUtil.toJson(errorData);
            response.getWriter().write(json);
        }
    }


    public static void logSuccess(String action, String context) {
        logger.info("SUCCESS: {} - {}", action, context);
    }


    public static void logFailure(String action, String context, Exception e) {
        logger.error("FAILURE: {} - {} Error: {}", action, context, e.getMessage(), e);
    }


    public static void logSecurityEvent(String eventType, String userId, String details) {
        logger.warn("SECURITY_EVENT [{}] User: {} Details: {}", eventType, userId, details);
    }
}
