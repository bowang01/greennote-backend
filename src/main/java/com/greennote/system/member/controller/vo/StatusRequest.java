package com.greennote.system.member.controller.vo;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record StatusRequest(@Min(0) @Max(1) int status) {
}
