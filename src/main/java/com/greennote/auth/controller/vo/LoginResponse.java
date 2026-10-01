package com.greennote.auth.controller.vo;

import java.time.Instant;

public record LoginResponse(Long userId, String username, String accessToken, Instant expiresAt) {
}
