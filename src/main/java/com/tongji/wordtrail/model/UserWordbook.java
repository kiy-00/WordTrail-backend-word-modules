package com.tongji.wordtrail.model;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;
import java.util.List;

/**
 * 用户词书实体类
 */
@Document(collection = "user_wordbooks")
public class UserWordbook {
    @Id
    private ObjectId id;
    private String language;
    private String bookName;
    private String description;
    private String createUser;
    private List<ObjectId> words;
    private boolean isPublic;
    private String status;  // "pending", "approved", "rejected"
    private List<String> tags;
    private Date createTime;

    // Constructors
    public UserWordbook() {
        this.createTime = new Date();
        this.status = "pending";
    }

    public UserWordbook(String language, String bookName, String description,
                        String createUser, List<ObjectId> words, boolean isPublic,
                        List<String> tags) {
        this();
        this.language = language;
        this.bookName = bookName;
        this.description = description;
        this.createUser = createUser;
        this.words = words;
        this.isPublic = isPublic;
        this.tags = tags;
    }

    // Getters and Setters
    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getBookName() {
        return bookName;
    }

    public void setBookName(String bookName) {
        this.bookName = bookName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCreateUser() {
        return createUser;
    }

    public void setCreateUser(String createUser) {
        this.createUser = createUser;
    }

    public List<ObjectId> getWords() {
        return words;
    }

    public void setWords(List<ObjectId> words) {
        this.words = words;
    }

    public boolean isPublic() {
        return isPublic;
    }

    public void setPublic(boolean isPublic) {
        this.isPublic = isPublic;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }
}