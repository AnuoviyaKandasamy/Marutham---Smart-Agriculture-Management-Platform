package com.marutham.controller;

import com.marutham.dao.CommentDAO;
import com.marutham.model.Comment;
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

@WebServlet("/comments")
public class CommentServlet extends HttpServlet {
    private CommentDAO commentDAO = new CommentDAO();

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        int postId = Integer.parseInt(request.getParameter("postId"));
        JsonUtil.sendAsJson(response, commentDAO.getCommentsByPost(postId));
    }

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
        String content = request.getParameter("content");

        Comment comment = new Comment();
        comment.setPostId(postId);
        comment.setUserId(userId);
        comment.setContent(content);

        if (commentDAO.addComment(comment)) {
            result.put("success", true);
        } else {
            result.put("success", false);
            result.put("message", "Failed to add comment");
        }
        JsonUtil.sendAsJson(response, result);
    }
}