package com.marutham.controller;

import com.marutham.util.JsonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.Properties;

@WebServlet("/translate")
public class TranslationServlet extends HttpServlet {
    private static final Logger logger = LoggerFactory.getLogger(TranslationServlet.class);

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String lang = request.getParameter("lang");
        if (lang == null || lang.isEmpty()) {

            lang = "en";
        }

        Properties messages = new Properties();
        String resourceFileName = "messages_" + lang.toLowerCase() + ".properties";

        try (InputStream input = getServletContext().getResourceAsStream("/WEB-INF/classes/" + resourceFileName)) {
            if (input == null) {
                logger.warn("Translation file not found for language: {}. Falling back to English.", lang);
                try (InputStream enInput = getServletContext().getResourceAsStream("/WEB-INF/classes/messages_en.properties")) {
                    if (enInput != null) {
                        messages.load(enInput);
                    } else {
                        logger.error("Default English translation file messages_en.properties not found!");
                    }
                }
            } else {
                messages.load(input);
            }
        } catch (IOException e) {
            logger.error("Error loading translation properties for language: {}", lang, e);
        }

        PrintWriter out = response.getWriter();
        out.print(JsonUtil.toJson(messages));
        out.flush();
    }
}
