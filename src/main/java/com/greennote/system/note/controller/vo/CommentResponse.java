package com.greennote.system.note.controller.vo;

public record CommentResponse(
        String id,
        String userId,
        String authorName,
        String parentId,
        String replyToUserId,
        String replyToName,
        String content,
        int likeCount,
        String createdAt,
        String updatedAt
) {
}
