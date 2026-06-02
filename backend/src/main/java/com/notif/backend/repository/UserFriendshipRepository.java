package com.notif.backend.repository;

import com.notif.backend.entity.UserFriendship;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UserFriendshipRepository extends JpaRepository<UserFriendship, Long> {

    @Query("""
    SELECT COUNT(f) > 0 FROM UserFriendship f
    WHERE (f.requester.id = :requesterId AND f.addressee.id = :addresseeId)
       OR (f.requester.id = :addresseeId AND f.addressee.id = :requesterId)
""")
    boolean existsByUsers(@Param("requesterId") Long requesterId,
                          @Param("addresseeId") Long addresseeId);

    @Query("""
    SELECT f FROM UserFriendship f
    WHERE f.status = 'ACCEPTED'
    AND (f.requester.id = :userId OR f.addressee.id = :userId)
    """)
    List<UserFriendship> findAcceptedFriendships(@Param("userId") Long userId);

    @Query("""
    SELECT f FROM UserFriendship f
    WHERE f.status = 'PENDING'
    AND f.addressee.id = :userId
    """)
    List<UserFriendship> findPendingRequestsForAddressee(@Param("userId") Long userId);

    boolean existsByAddressee_id(Long friendshipId);
}
