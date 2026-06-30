package com.marutham.controller;

import com.marutham.dao.PostDAO;
import com.marutham.model.Post;
import com.marutham.util.JsonUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet("/posts")
public class PostServlet extends HttpServlet {
    private PostDAO postDAO = new PostDAO();

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        int currentUserId = (session != null && session.getAttribute("userId") != null)
                ? (int) session.getAttribute("userId") : 0;

        String search   = request.getParameter("search");
        String category = request.getParameter("category");

        List<Post> posts;
        if (search != null && !search.trim().isEmpty()) {
            posts = postDAO.searchPosts(search.trim(), currentUserId);
        } else if (category != null && !category.isEmpty()) {
            posts = postDAO.getPostsByCategory(category, currentUserId);
        } else {
            posts = postDAO.getAllPosts(currentUserId);
        }
        JsonUtil.sendAsJson(response, posts);
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        Map<String, Object> result = new HashMap<>();

        if (session == null || session.getAttribute("userId") == null) {
            result.put("success", false); result.put("message", "Unauthorized");
            JsonUtil.sendAsJson(response, result); return;
        }

        int userId = (int) session.getAttribute("userId");
        String role   = (String) session.getAttribute("role");
        String action = request.getParameter("action");

        if ("update".equals(action)) {
            int postId      = Integer.parseInt(request.getParameter("postId"));
            String title    = request.getParameter("title");
            String content  = request.getParameter("content");
            String category = request.getParameter("category");
            boolean ok = postDAO.updatePost(postId, userId, title, content, category);
            result.put("success", ok);
            if (!ok) result.put("message", "Unable to update post (not the owner?)");
        } else if ("delete".equals(action)) {
            int postId   = Integer.parseInt(request.getParameter("postId"));
            boolean isAdmin = "ADMIN".equals(role);
            boolean ok = postDAO.deletePost(postId, userId, isAdmin);
            result.put("success", ok);
            if (!ok) result.put("message", "Unable to delete post (not the owner?)");
        } else {
            String title    = request.getParameter("title");
            String content  = request.getParameter("content");
            String category = request.getParameter("category");

            Post post = new Post();
            post.setUserId(userId);
            post.setTitle(title);
            post.setContent(content);
            post.setCategory(category);

            if (postDAO.createPost(post)) {
                result.put("success", true);
            } else {
                result.put("success", false); result.put("message", "Failed to create post");
            }
        }
        JsonUtil.sendAsJson(response, result);
    }
}
