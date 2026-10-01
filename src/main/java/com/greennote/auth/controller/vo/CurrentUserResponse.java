package com.greennote.auth.controller.vo;

import java.util.List;

public record CurrentUserResponse(Long userId, String username, String nickname, List<MenuItem> menus) {

    public record MenuItem(Long id, String name, String path) {
    }
}
