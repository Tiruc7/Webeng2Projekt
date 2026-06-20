package com.notif.backend.dto;

import java.time.LocalDateTime;

public record CommentDTO(
        Long id,
        Long userId,
        String username,
        String eventId,
        String content,
        LocalDateTime createdAt
) {
}