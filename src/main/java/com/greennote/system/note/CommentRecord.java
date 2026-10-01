package com.greennote.system.note;

import java.time.LocalDateTime;

public record CommentRecord(
        String id,
        String noteId,
        String userId,
        String authorName,
        String parentId,
        String replyToUserId,
        String replyToName,
        String content,
        int likeCount,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        int deleted
) {
}
