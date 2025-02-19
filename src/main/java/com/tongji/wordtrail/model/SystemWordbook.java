package com.tongji.wordtrail.model;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

/**
 * 系统词书实体类
 */
@Document(collection = "system_wordbooks")
public class SystemWordbook {
    @Id
    private ObjectId id;
    private String language;
    private String bookName;
    private String description;
    private String createUser = "system";  // 固定为"system"
    private List<ObjectId> words;

    // Constructors
    public SystemWordbook() {}

    public SystemWordbook(String language, String bookName, String description, List<ObjectId> words) {
        this.language = language;
        this.bookName = bookName;
        this.description = description;
        this.words = words;
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

    public List<ObjectId> getWords() {
        return words;
    }

    public void setWords(List<ObjectId> words) {
        this.words = words;
    }
}