package com.tongji.wordtrail.dto;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class CommentRequest {
    private String postId;
    private String content;
    private String userId;
    private LocalDateTime createdTime;
    private LocalDateTime updatedTime;
    private String parentComment;
    private String replyToName;
    private boolean isMyComment;
    private String avatar;
}
