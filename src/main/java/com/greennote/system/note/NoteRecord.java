package com.greennote.system.note;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record NoteRecord(
        String id,
        String userId,
        String authorName,
        String authorAvatar,
        String channelId,
        String channelName,
        int type,
        String title,
        String content,
        String coverUrl,
        String mediaJson,
        String videoUrl,
        String placeName,
        String cityName,
        BigDecimal longitude,
        BigDecimal latitude,
        int status,
        String rejectReason,
        int likeCount,
        int collectCount,
        int commentCount,
        LocalDateTime publishedAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        int deleted
) {
}
