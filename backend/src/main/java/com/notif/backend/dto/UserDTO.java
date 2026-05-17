package com.notif.backend.dto;

public record UserDTO(
        Long id,
        String username,
        String external_id
) {
}
