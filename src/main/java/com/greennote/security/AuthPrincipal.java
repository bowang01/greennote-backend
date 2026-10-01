package com.greennote.security;

public record AuthPrincipal(Long userId, String username, String kind) {

    public boolean admin() {
        return "admin".equals(kind);
    }

    public boolean member() {
        return "member".equals(kind);
    }
}
