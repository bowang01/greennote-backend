package com.greennote.system.topic.controller.vo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record TopicSaveRequest(
        @NotBlank @Size(max = 64) String name,
        @Size(max = 255) String intro,
        int sortNo
) {
}
