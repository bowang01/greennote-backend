package com.greennote.auth.controller.vo;

import java.util.List;

public record CurrentUserResponse(String userId, String username, String nickname, List<MenuItem> menus) {

    public record MenuItem(String id, String name, String path) {
    }
}
