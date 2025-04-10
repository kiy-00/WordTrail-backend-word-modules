package com.tongji.wordtrail.model;

import lombok.Data;
import javax.persistence.*;
import java.util.Date;

@Data
@Entity
@Table(name = "team_challenge_daily_result")
public class TeamChallengeDailyResult {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "challenge_id", nullable = false)
    private Long challengeId;

    @Column(name = "result_date", nullable = false)
    @Temporal(TemporalType.DATE)
    private Date resultDate;

    @Column(name = "both_completed", nullable = false)
    private boolean bothCompleted;

    @Column(name = "create_time")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createTime;

    public TeamChallengeDailyResult() {
        this.createTime = new Date();
    }
}