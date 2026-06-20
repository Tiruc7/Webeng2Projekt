package com.notif.backend.dto;

import org.springframework.lang.NonNull;

public record EventDTO(
        @NonNull String id,
        String title,
        String venue,
        String city,
        String date,
        String time,
        String imageUrl,
        String ticketUrl,
        String status
) {
}