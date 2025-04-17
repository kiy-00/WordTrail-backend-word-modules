package com.tongji.wordtrail.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "favourite") // MongoDB 的集合
public class Favourite {
    @Id
    private String id;
    private String userId;
    private String postId;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    public Favourite(String userId, String postId, LocalDateTime createTime, LocalDateTime updateTime) {
        this.userId = userId;
        this.postId = postId;
        this.createTime = createTime;
        this.updateTime = updateTime;
    }
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    public String getUserId() {
        return userId;
    }
    public void setUserId(String userId) {
        this.userId = userId;
    }
    public String getPostId() {
        return postId;
    }
    public void setPostId(String postId) {
        this.postId = postId;
    }
    public LocalDateTime getCreateTime() {
        return createTime;
    }
    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }
    public LocalDateTime getUpdateTime() {
        return updateTime;
    }
    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }
}
