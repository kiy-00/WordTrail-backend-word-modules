package com.tongji.wordtrail.model;

import lombok.Data;
import javax.persistence.*;
import java.util.Date;

@Data
@Entity
@Table(name = "team_challenge_clock_in")
public class TeamChallengeClockIn {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "challenge_id", nullable = false)
    private Long challengeId;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "clock_in_date", nullable = false)
    @Temporal(TemporalType.DATE)
    private Date clockInDate;

    @Column(name = "words_completed", nullable = false)
    private int wordsCompleted;

    @Column(nullable = false)
    private boolean status;

    @Column(name = "create_time")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createTime;

    @Column(name = "update_time")
    @Temporal(TemporalType.TIMESTAMP)
    private Date updateTime;

    // 构造函数
    public TeamChallengeClockIn() {
        this.createTime = new Date();
        this.updateTime = new Date();
        this.status = false;
        this.wordsCompleted = 0;
    }

    // 自定义构造函数
    public TeamChallengeClockIn(Long challengeId, String userId, Date clockInDate) {
        this.challengeId = challengeId;
        this.userId = userId;
        this.clockInDate = clockInDate;
        this.status = false;
        this.wordsCompleted = 0;
        this.createTime = new Date();
        this.updateTime = new Date();
    }
}