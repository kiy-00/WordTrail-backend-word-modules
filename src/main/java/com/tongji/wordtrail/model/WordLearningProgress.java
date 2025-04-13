package com.tongji.wordtrail.model;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Document(collection = "word_learning_progress")
public class WordLearningProgress {
    @Id
    @JsonSerialize(using = ToStringSerializer.class)
    private ObjectId id;

    private String userId;

    @JsonSerialize(using = ToStringSerializer.class)
    private ObjectId wordId;

    private double proficiency;
    private Date lastReviewTime;
    private Date firstLearnTime;
    private Date nextReviewTime;
    private int reviewStage;
    private List<ReviewRecord> reviewHistory;

    // 添加一个新字段，标记数据是否来自数据库
    private boolean fromDatabase = true;

    public WordLearningProgress() {
        this.proficiency = 0.0;
        this.reviewStage = 0;
        this.reviewHistory = new ArrayList<>();
    }

    public WordLearningProgress(String userId, ObjectId wordId) {
        this();
        this.userId = userId;
        this.wordId = wordId;
        this.firstLearnTime = new Date();
        this.lastReviewTime = new Date();
        this.nextReviewTime = new Date();
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

    public ObjectId getWordId() {
        return wordId;
    }

    public void setWordId(ObjectId wordId) {
        this.wordId = wordId;
    }

    public double getProficiency() {
        return proficiency;
    }

    public void setProficiency(double proficiency) {
        this.proficiency = proficiency;
    }

    public Date getLastReviewTime() {
        return lastReviewTime;
    }

    public void setLastReviewTime(Date lastReviewTime) {
        this.lastReviewTime = lastReviewTime;
    }

    public Date getFirstLearnTime() {
        return firstLearnTime;
    }

    public void setFirstLearnTime(Date firstLearnTime) {
        this.firstLearnTime = firstLearnTime;
    }

    public Date getNextReviewTime() {
        return nextReviewTime;
    }

    public void setNextReviewTime(Date nextReviewTime) {
        this.nextReviewTime = nextReviewTime;
    }

    public int getReviewStage() {
        return reviewStage;
    }

    public void setReviewStage(int reviewStage) {
        this.reviewStage = reviewStage;
    }

    public List<ReviewRecord> getReviewHistory() {
        return reviewHistory;
    }

    public void setReviewHistory(List<ReviewRecord> reviewHistory) {
        this.reviewHistory = reviewHistory;
    }

    public void addReviewHistory(boolean remembered) {
        ReviewRecord record = new ReviewRecord(new Date(), remembered);
        if (this.reviewHistory == null) {
            this.reviewHistory = new ArrayList<>();
        }
        this.reviewHistory.add(record);
        this.lastReviewTime = record.getReviewTime();
    }

    // 添加 fromDatabase 相关的 getter 和 setter
    public boolean isFromDatabase() {
        return fromDatabase;
    }

    public void setFromDatabase(boolean fromDatabase) {
        this.fromDatabase = fromDatabase;
    }

    // 内部类：复习记录
    public static class ReviewRecord {
        private Date reviewTime;
        private boolean remembered;

        public ReviewRecord() {
        }

        public ReviewRecord(Date reviewTime, boolean remembered) {
            this.reviewTime = reviewTime;
            this.remembered = remembered;
        }

        public Date getReviewTime() {
            return reviewTime;
        }

        public void setReviewTime(Date reviewTime) {
            this.reviewTime = reviewTime;
        }

        public boolean isRemembered() {
            return remembered;
        }

        public void setRemembered(boolean remembered) {
            this.remembered = remembered;
        }
    }
}