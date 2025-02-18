package com.tongji.wordtrail.model;

import javax.persistence.*;
import lombok.Data;
import java.util.Date;

@Data
@Entity
@Table(name = "user_checkin")
public class UserCheckin {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "checkin_date", nullable = false)
    @Temporal(TemporalType.DATE)
    private Date checkinDate;

    @Column(name = "checkin_days", nullable = false)
    private Integer checkinDays;

    @Column(name = "create_time")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createTime;

    @ManyToOne
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User user;
}