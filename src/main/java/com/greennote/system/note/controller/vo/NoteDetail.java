package com.greennote.system.note.controller.vo;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record NoteDetail(
        String id,
        String authorId,
        String authorName,
        String authorAvatar,
        String channelId,
        String channelName,
        int type,
        String title,
        String content,
        String coverUrl,
        List<String> imageUrls,
        String videoUrl,
        List<TopicBrief> topics,
        String placeName,
        String cityName,
        BigDecimal longitude,
        BigDecimal latitude,
        int status,
        String rejectReason,
        int likeCount,
        int collectCount,
        int commentCount,
        boolean liked,
        boolean collected,
        LocalDateTime publishedAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
