package com.marutham.controller;

import com.marutham.util.JsonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

@WebServlet("/weather")
public class WeatherServlet extends HttpServlet {
    private static final Logger logger = LoggerFactory.getLogger(WeatherServlet.class);
    private static final String API_KEY = System.getenv("OPENWEATHER_API_KEY");

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String lat = request.getParameter("lat");
        String lon = request.getParameter("lon");

        if (lat == null || lon == null || lat.trim().isEmpty() || lon.trim().isEmpty()) {
            JsonUtil.sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Latitude and Longitude are required.");
            return;
        }

        try {
            Double.parseDouble(lat);
            Double.parseDouble(lon);
        } catch (NumberFormatException e) {
            JsonUtil.sendError(response, HttpServletResponse.SC_BAD_REQUEST, "Invalid latitude or longitude.");
            return;
        }

        if (API_KEY == null || API_KEY.isEmpty()) {
            logger.warn("OpenWeatherMap API Key not found. Sending mock data.");
            sendMockWeather(response);
            return;
        }

        try {
            String currentWeatherUrl = "https://api.openweathermap.org/data/2.5/weather?lat=" + lat + "&lon=" + lon + "&units=metric&appid=" + API_KEY;
            String forecastUrl      = "https://api.openweathermap.org/data/2.5/forecast?lat=" + lat + "&lon=" + lon + "&units=metric&appid=" + API_KEY;

            String currentRes  = fetchUrl(currentWeatherUrl);
            String forecastRes = fetchUrl(forecastUrl);

            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write("{\"current\":" + currentRes + ", \"forecast\":" + forecastRes + "}");

        } catch (Exception e) {
            logger.error("Error fetching weather data", e);
            sendMockWeather(response);
        }
    }

    private String fetchUrl(String urlString) throws IOException {
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(5000);
        conn.setReadTimeout(5000);
        StringBuilder result = new StringBuilder();
        try (BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
            String line;
            while ((line = rd.readLine()) != null) result.append(line);
        }
        return result.toString();
    }

    private void sendMockWeather(HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        String mockData = "{"
            + "\"current\": {\"main\": {\"temp\": 30.5, \"humidity\": 65}, \"weather\": [{\"main\": \"Clear\", \"description\": \"clear sky\"}], \"name\": \"Mock City\"},"
            + "\"forecast\": {\"list\": [{\"dt_txt\": \"Tomorrow\", \"main\": {\"temp\": 28.0}, \"weather\": [{\"main\": \"Rain\", \"description\": \"light rain\"}]}]}"
            + "}";
        response.getWriter().write(mockData);
    }
}
