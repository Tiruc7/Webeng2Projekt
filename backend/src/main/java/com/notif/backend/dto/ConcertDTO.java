package com.notif.backend.dto;
//Interne API-Datenstruktur, diese Felder gehen ans Frontend, nicht mehr alles
public record ConcertDTO(
        String id,
        String title,
        String venue,
        String city,
        String date,
        String time,
        String imageUrl
) {
}