package com.greennote.system.channel.controller.vo;

import java.time.LocalDateTime;

public record ChannelResponse(
        String id,
        String name,
        int sortNo,
        int status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
