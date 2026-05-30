package com.notif.backend.repository;

import com.notif.backend.entity.UserFriendship;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserFrienshipRepository extends JpaRepository<UserFriendship, Long> {
    boolean existsByUsers(Long requesterId, Long addresseeId);
}
