package com.tongji.wordtrail.dto;

import lombok.Data;

@Data
public class PostResponse {
    String id;
    String createdTime;
    String updatedTime;
    String title;
    String content;
    String author;
    String userAvatarUrl;
    int commentCount;
    int voteCount;

    public PostResponse() {}

    public PostResponse(String id, String createdTime, String updatedTime, String title, String content, String author, String userAvatarUrl, int commentCount, int voteCount) {
        this.id = id;
        this.createdTime = createdTime;
        this.updatedTime = updatedTime;
        this.title = title;
        this.content = content;
        this.author = author;
        this.userAvatarUrl = userAvatarUrl;
        this.commentCount = commentCount;
        this.voteCount = voteCount;
    }
}
