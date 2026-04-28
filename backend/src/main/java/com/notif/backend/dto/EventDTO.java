package com.notif.backend.dto;
public record EventDTO(
        String id,
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