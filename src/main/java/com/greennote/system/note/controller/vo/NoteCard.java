package com.greennote.system.note.controller.vo;

public record NoteCard(
        String id,
        String title,
        String coverUrl,
        String authorName,
        String authorAvatar,
        int likeCount,
        int status
) {
}
