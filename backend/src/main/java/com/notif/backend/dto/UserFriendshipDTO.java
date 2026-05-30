package com.notif.backend.dto;

import com.notif.backend.entity.User;
import com.notif.backend.enums.FriendshipStatus;

import java.time.LocalDateTime;

public record UserFriendshipDTO(
        Long id,
        User requester,
        User addressee,
        FriendshipStatus status,
        LocalDateTime createdAt
) {

}
