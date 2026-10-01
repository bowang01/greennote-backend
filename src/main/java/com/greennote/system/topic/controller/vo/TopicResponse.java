package com.greennote.system.topic.controller.vo;

import java.time.LocalDateTime;

public record TopicResponse(
        String id,
        String name,
        String intro,
        int sortNo,
        int status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
