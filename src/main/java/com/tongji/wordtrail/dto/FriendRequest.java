package com.tongji.wordtrail.dto;

import lombok.Data;
import javax.persistence.*;
import java.util.Date;

@Data
@Entity
@Table(name = "friend_request")
public class FriendRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "sender_id", nullable = false)
    private String senderId;

    @Column(name = "receiver_id", nullable = false)
    private String receiverId;

    @Column(nullable = false)
    private String status; // pending, accepted, rejected

    @Column
    private String message;

    @Column(name = "create_time")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createTime;

    @Column(name = "update_time")
    @Temporal(TemporalType.TIMESTAMP)
    private Date updateTime;

    // 构造函数
    public FriendRequest() {
        this.createTime = new Date();
        this.updateTime = new Date();
        this.status = "pending";
    }

    // 自定义构造函数
    public FriendRequest(String senderId, String receiverId, String message) {
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.message = message;
        this.status = "pending";
        this.createTime = new Date();
        this.updateTime = new Date();
    }
}