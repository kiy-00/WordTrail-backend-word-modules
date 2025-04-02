package com.tongji.wordtrail.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;
import java.util.List;

@Document(collection = "posts") // MongoDB 的集合
public class Post {

    @Id
    private String id; // MongoDB 使用 String 作为主键
    private String title;
    private String content;
    private List<String> filePaths; // 存储文件路径列表
    private LocalDateTime createdAt;

    public Post() {}

    public Post(String title, String content, List<String> filePaths, LocalDateTime createdAt) {
        this.title = title;
        this.content = content;
        this.filePaths = filePaths;
        this.createdAt = createdAt;
    }

    // Getter 和 Setter
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public List<String> getFilePaths(List<String> filePaths) {
        return filePaths;
    }

    public void setFilePaths(List<String> filePaths) {
        this.filePaths = filePaths;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}

