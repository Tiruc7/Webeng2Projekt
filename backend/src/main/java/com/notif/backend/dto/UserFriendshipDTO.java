package com.notif.backend.dto;

import com.notif.backend.entity.UserFriendship;
import com.notif.backend.enums.FriendshipStatus;

import java.time.LocalDateTime;

public record UserFriendshipDTO(
        Long friendshipId,
        UserDTO requester,
        UserDTO addressee,
        FriendshipStatus status,
        LocalDateTime createdAt
) {
    public static UserFriendshipDTO from(UserFriendship f) {
        return new UserFriendshipDTO(
                f.getId(),
                f.getRequester().toDTO(),
                f.getAddressee().toDTO(),
                f.getStatus(),
                f.getCreatedAt()
        );
    }
}
