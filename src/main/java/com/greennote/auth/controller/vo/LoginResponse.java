package com.greennote.auth.controller.vo;

import java.time.Instant;

public record LoginResponse(String userId, String username, String accessToken, Instant expiresAt) {
}
