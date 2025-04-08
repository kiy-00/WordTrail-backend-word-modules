package com.tongji.wordtrail.model;

import javax.persistence.*;
import lombok.Data;
import java.util.Date;

@Data
@Entity
@Table(name = "administer")
public class Administer {
    @Id
    @Column(name = "user_id")
    private String userId;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(name = "admin_key", unique = true, nullable = false)
    private String adminKey;

    @Column(unique = true)
    private String avatarUrl;
}
