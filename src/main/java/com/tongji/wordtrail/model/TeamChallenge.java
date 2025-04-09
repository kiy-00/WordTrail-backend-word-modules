package com.tongji.wordtrail.model;

import lombok.Data;
import javax.persistence.*;
import java.util.Date;

@Data
@Entity
@Table(name = "team_challenge")
public class TeamChallenge {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "creator_id", nullable = false)
    private String creatorId;

    @Column(name = "partner_id", nullable = false)
    private String partnerId;

    @Column(nullable = false)
    private String name;

    @Column
    private String description;

    @Column(nullable = false)
    private String status; // pending, active, completed, failed

    @Column(name = "daily_words_target", nullable = false)
    private int dailyWordsTarget;

    @Column(name = "streak_days", nullable = false)
    private int streakDays;

    @Column(name = "start_date")
    @Temporal(TemporalType.DATE)
    private Date startDate;

    @Column(name = "end_date")
    @Temporal(TemporalType.DATE)
    private Date endDate;

    @Column(name = "create_time")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createTime;

    @Column(name = "update_time")
    @Temporal(TemporalType.TIMESTAMP)
    private Date updateTime;

    // 构造函数
    public TeamChallenge() {
        this.createTime = new Date();
        this.updateTime = new Date();
        this.status = "pending";
        this.streakDays = 0;
    }
}