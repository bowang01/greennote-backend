package com.greennote.system.note.controller.vo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.List;

public record NoteSaveRequest(
        @NotBlank @Size(max = 128) String title,
        @Size(max = 20000) String content,
        String channelId,
        List<String> topicIds,
        List<String> imageUrls,
        Integer type,
        @Size(max = 512) String videoUrl,
        @Size(max = 128) String placeName,
        @Size(max = 64) String cityName,
        BigDecimal longitude,
        BigDecimal latitude
) {
}
