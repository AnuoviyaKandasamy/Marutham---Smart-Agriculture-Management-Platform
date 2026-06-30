package com.marutham.model;

import java.sql.Timestamp;

public class Comment {
    private int commentId;
    private int postId;
    private int userId;
    private String username;
    private String content;
    private Timestamp createdAt;

    public int getCommentId() { return commentId; }
    public void setCommentId(int commentId) { this.commentId = commentId; }
    public int getPostId() { return postId; }
    public void setPostId(int postId) { this.postId = postId; }
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
}
