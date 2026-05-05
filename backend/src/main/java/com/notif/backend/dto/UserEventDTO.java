package com.notif.backend.dto;

public record UserEventDTO(
        String id,
        UserDTO user,
        EventDTO event
) {

}
