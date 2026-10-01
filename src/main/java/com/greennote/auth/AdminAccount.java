package com.greennote.auth;

public record AdminAccount(Long id, String username, String password, String nickname, int status) {
}
