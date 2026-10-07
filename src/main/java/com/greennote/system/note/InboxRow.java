package com.greennote.system.note;

import java.time.LocalDateTime;

public record InboxRow(
        String id,
        String kind,
        String actorName,
        String actorAvatar,
        String noteId,
        String noteTitle,
        String text,
        LocalDateTime createdAt
) {
}
