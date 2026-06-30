package com.marutham.controller;

import com.marutham.dao.LikeDAO;
import com.marutham.util.JsonUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@WebServlet("/likes")
public class LikeServlet extends HttpServlet {
    private final LikeDAO likeDAO = new LikeDAO();

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        Map<String, Object> result = new HashMap<>();

        if (session == null || session.getAttribute("userId") == null) {
            result.put("success", false);
            result.put("message", "Unauthorized");
            JsonUtil.sendAsJson(response, result);
            return;
        }

        int userId = (int) session.getAttribute("userId");
        int postId = Integer.parseInt(request.getParameter("postId"));

        boolean nowLiked = likeDAO.toggleLike(postId, userId);
        result.put("success", true);
        result.put("liked", nowLiked);
        result.put("likeCount", likeDAO.getLikeCount(postId));
        JsonUtil.sendAsJson(response, result);
    }
}