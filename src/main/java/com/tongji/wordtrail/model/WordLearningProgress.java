package com.tongji.wordtrail.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.bson.types.ObjectId;
import java.util.Date;

@Document(collection = "word_learning_progress")
@CompoundIndexes({
        @CompoundIndex(name = "userId_wordId_idx", def = "{'userId': 1, 'wordId': 1}", unique = true),
        @CompoundIndex(name = "userId_proficiency_idx", def = "{'userId': 1, 'proficiency': 1}")
})
public class WordLearningProgress {
    @Id
    private ObjectId id;

    @Indexed
    private ObjectId userId;

    private ObjectId wordId;

    private double proficiency;

    private Date lastReviewTime;

    // Default constructor
    public WordLearningProgress() {
    }

    // Constructor with fields
    public WordLearningProgress(ObjectId userId, ObjectId wordId, double proficiency, Date lastReviewTime) {
        this.userId = userId;
        this.wordId = wordId;
        this.proficiency = proficiency;
        this.lastReviewTime = lastReviewTime;
    }

    // Getters and Setters
    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public ObjectId getUserId() {
        return userId;
    }

    public void setUserId(ObjectId userId) {
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

    @Override
    public String toString() {
        return "WordLearningProgress{" +
                "id=" + id +
                ", userId=" + userId +
                ", wordId=" + wordId +
                ", proficiency=" + proficiency +
                ", lastReviewTime=" + lastReviewTime +
                '}';
    }
}