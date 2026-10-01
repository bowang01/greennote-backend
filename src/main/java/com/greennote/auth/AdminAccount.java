package com.greennote.auth;

public record AdminAccount(String id, String username, String password, String nickname, int status) {
}
