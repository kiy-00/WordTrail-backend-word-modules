package com.tongji.wordtrail.model;

import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.annotation.Id;

import java.time.LocalDateTime;

@Document(collection = "vote")
public class Vote {
    @Id
    private String id;
    private LocalDateTime voteTime;
    private String userId;
    private String upvote;
    private String postId;
    public Vote(LocalDateTime voteTime, String userId, String upvote, String postId) {
        this.voteTime = voteTime;
        this.userId = userId;
        this.upvote = upvote;
        this.postId = postId;
    }
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    public LocalDateTime getVoteTime() {
        return voteTime;
    }
    public void setVoteTime(LocalDateTime voteTime) {
        this.voteTime = voteTime;
    }
    public String getUserId() {
        return userId;
    }
    public void setUserId(String userId) {
        this.userId = userId;
    }
    public String getUpvote() {
        return upvote;
    }
    public void setUpvote(String upvote) {
        this.upvote = upvote;
    }
    public String getPostId() {
        return postId;
    }
    public void setPostId(String postId) {
        this.postId = postId;
    }

}
