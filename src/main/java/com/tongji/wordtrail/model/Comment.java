package com.tongji.wordtrail.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "comment") // MongoDB 的集合
public class Comment {
    @Id
    private String id;
    private String postId;
    private String content;
    private String userId;
}
