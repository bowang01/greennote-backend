package com.greennote.system.member.controller.vo;

public record ProfileResponse(String userId, String username, String nickname, String avatar, String bio, int status) {
}
