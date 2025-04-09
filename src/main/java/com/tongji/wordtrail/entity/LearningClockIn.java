package com.tongji.wordtrail.entity;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "learning_clock_in")
public class LearningClockIn {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "user_id", nullable = false, length = 36)
    private String userId;

    @Column(name = "clock_in_date", nullable = false)
    private Date clockInDate;

    @Column(name = "status", nullable = false)
    private Boolean status; // 0-未达标，1-达标

    @Column(name = "new_words_target", nullable = false)
    private Integer newWordsTarget;

    @Column(name = "new_words_completed", nullable = false)
    private Integer newWordsCompleted;

    @Column(name = "review_words_target", nullable = false)
    private Integer reviewWordsTarget;

    @Column(name = "review_words_completed", nullable = false)
    private Integer reviewWordsCompleted;

    @Column(name = "streak_days", nullable = false)
    private Integer streakDays;

    @Column(name = "create_time", nullable = false)
    private Date createTime;

    @Column(name = "update_time", nullable = false)
    private Date updateTime;

    // 默认构造函数
    public LearningClockIn() {
        this.status = false;
        this.newWordsTarget = 0;
        this.newWordsCompleted = 0;
        this.reviewWordsTarget = 0;
        this.reviewWordsCompleted = 0;
        this.streakDays = 0;
        this.createTime = new Date();
        this.updateTime = new Date();
    }

    // 有参构造函数
    public LearningClockIn(String userId, Date clockInDate) {
        this();
        this.userId = userId;
        this.clockInDate = clockInDate;
    }

    // Getter and Setter methods
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Date getClockInDate() {
        return clockInDate;
    }

    public void setClockInDate(Date clockInDate) {
        this.clockInDate = clockInDate;
    }

    public Boolean getStatus() {
        return status;
    }

    public void setStatus(Boolean status) {
        this.status = status;
    }

    public Integer getNewWordsTarget() {
        return newWordsTarget;
    }

    public void setNewWordsTarget(Integer newWordsTarget) {
        this.newWordsTarget = newWordsTarget;
    }

    public Integer getNewWordsCompleted() {
        return newWordsCompleted;
    }

    public void setNewWordsCompleted(Integer newWordsCompleted) {
        this.newWordsCompleted = newWordsCompleted;
    }

    public Integer getReviewWordsTarget() {
        return reviewWordsTarget;
    }

    public void setReviewWordsTarget(Integer reviewWordsTarget) {
        this.reviewWordsTarget = reviewWordsTarget;
    }

    public Integer getReviewWordsCompleted() {
        return reviewWordsCompleted;
    }

    public void setReviewWordsCompleted(Integer reviewWordsCompleted) {
        this.reviewWordsCompleted = reviewWordsCompleted;
    }

    public Integer getStreakDays() {
        return streakDays;
    }

    public void setStreakDays(Integer streakDays) {
        this.streakDays = streakDays;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }
}