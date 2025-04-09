package com.tongji.wordtrail.model;

import lombok.Data;
import javax.persistence.*;
import java.util.Date;

@Data
@Entity
@Table(name = "user_friend")
public class UserFriend {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "friend_id", nullable = false)
    private String friendId;

    @Column(name = "nickname")
    private String nickname;

    @Column(nullable = false)
    private String status; // active, blocked

    @Column(name = "create_time")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createTime;

    @Column(name = "update_time")
    @Temporal(TemporalType.TIMESTAMP)
    private Date updateTime;

    // 构造函数
    public UserFriend() {
        this.createTime = new Date();
        this.updateTime = new Date();
        this.status = "active";
    }

    // 自定义构造函数创建好友关系
    public UserFriend(String userId, String friendId) {
        this.userId = userId;
        this.friendId = friendId;
        this.createTime = new Date();
        this.updateTime = new Date();
        this.status = "active";
    }
}