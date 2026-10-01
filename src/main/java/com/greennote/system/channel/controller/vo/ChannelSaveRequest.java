package com.greennote.system.channel.controller.vo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChannelSaveRequest(
        @NotBlank @Size(max = 32) String name,
        int sortNo
) {
}
