package com.greennote.system.member.controller.vo;

public record ProfileResponse(Long userId, String username, String nickname, String avatar, String bio, int status) {
}
