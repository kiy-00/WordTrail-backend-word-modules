package com.tongji.wordtrail.model;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

import java.util.Date;

@Document(collection = "learning_goals")
public class LearningGoal {
    @Id
    @JsonSerialize(using = ToStringSerializer.class)
    private ObjectId id;

    private String userId;
    private int dailyNewWordsGoal;
    private int dailyReviewWordsGoal;
    private Date createdAt;
    private Date updatedAt;

    public LearningGoal() {
        this.dailyNewWordsGoal = 10; // 默认值
        this.dailyReviewWordsGoal = 30; // 默认值
        this.createdAt = new Date();
        this.updatedAt = new Date();
    }

    public LearningGoal(String userId, int dailyNewWordsGoal, int dailyReviewWordsGoal) {
        this();
        this.userId = userId;
        this.dailyNewWordsGoal = dailyNewWordsGoal;
        this.dailyReviewWordsGoal = dailyReviewWordsGoal;
    }

    // Getter and Setter methods
    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public int getDailyNewWordsGoal() {
        return dailyNewWordsGoal;
    }

    public void setDailyNewWordsGoal(int dailyNewWordsGoal) {
        this.dailyNewWordsGoal = dailyNewWordsGoal;
    }

    public int getDailyReviewWordsGoal() {
        return dailyReviewWordsGoal;
    }

    public void setDailyReviewWordsGoal(int dailyReviewWordsGoal) {
        this.dailyReviewWordsGoal = dailyReviewWordsGoal;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }
}