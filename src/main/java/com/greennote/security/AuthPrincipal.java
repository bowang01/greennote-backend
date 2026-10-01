package com.greennote.security;

public record AuthPrincipal(String userId, String username, String kind) {

    public boolean admin() {
        return "admin".equals(kind);
    }

    public boolean member() {
        return "member".equals(kind);
    }
}
