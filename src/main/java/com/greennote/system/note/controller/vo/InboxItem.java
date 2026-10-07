package com.greennote.system.note.controller.vo;

public record InboxItem(
        String id,
        String kind,
        String actorName,
        String actorAvatar,
        String noteId,
        String noteTitle,
        String text,
        String createdAt
) {
}
