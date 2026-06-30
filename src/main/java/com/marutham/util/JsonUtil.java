package com.marutham.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

public class JsonUtil {
    private static final Logger logger = LoggerFactory.getLogger(JsonUtil.class);
    private static final Gson gson = new GsonBuilder()
            .setDateFormat("yyyy-MM-dd HH:mm:ss")
            .create();

    private JsonUtil() {}

    public static String toJson(Object obj) {
        return gson.toJson(obj);
    }

    public static void sendAsJson(HttpServletResponse response, Object obj) {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        try (PrintWriter out = response.getWriter()) {
            String jsonResponse = toJson(obj);
            out.print(jsonResponse);
            out.flush();
        } catch (IOException e) {
            logger.error("Error writing JSON response", e);
        }
    }

    public static void sendError(HttpServletResponse response, int statusCode, String message) {
        response.setStatus(statusCode);
        Map<String, Object> error = new HashMap<>();
        error.put("success", false);
        error.put("message", message);
        sendAsJson(response, error);
    }

    public static void sendSuccess(HttpServletResponse response, String message) {
        Map<String, Object> success = new HashMap<>();
        success.put("success", true);
        success.put("message", message);
        sendAsJson(response, success);
    }
}