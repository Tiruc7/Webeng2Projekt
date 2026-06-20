package com.notif.backend.Keycloak;

import com.notif.backend.repository.UserEventRepository;
import com.notif.backend.repository.UserFriendshipRepository;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component("guard")
public class OwnershipGuard {

    private final KeycloakUserHolder userHolder;
    private final UserEventRepository userEventRepo;
    private final UserFriendshipRepository userFriendshipRepository;

    public OwnershipGuard(KeycloakUserHolder userHolder, UserEventRepository userEventRepo, UserFriendshipRepository userFriendshipRepository) {
        this.userHolder = userHolder;
        this.userEventRepo = userEventRepo;
        this.userFriendshipRepository = userFriendshipRepository;
    }

    public boolean isUser(Authentication auth, Long userId) {
        return userHolder.getCurrentUser(auth).getId().equals(userId);
    }

    public boolean ownsUserEvent(Authentication auth, @NonNull String userEventId) {
        return userEventRepo.findById(userEventId)
                .map(ue -> {
                    Long currentUserId = userHolder.getCurrentUser(auth).getId();
                    return ue.getUser().getId().equals(currentUserId);
                })
                .orElse(false);
    }

    public boolean isFriendshipParticipant(Authentication auth, @NonNull Long friendshipId) {
        Long currentUserId = userHolder.getCurrentUser(auth).getId();
        return userFriendshipRepository.findById(friendshipId)
                .map(f -> f.getRequester().getId().equals(currentUserId)
                        || f.getAddressee().getId().equals(currentUserId))
                .orElse(false);
    }

    public boolean isFriendshipAddressee(Authentication auth, @NonNull Long friendshipId) {
        Long currentUserId = userHolder.getCurrentUser(auth).getId();
        return userFriendshipRepository.findById(friendshipId).map(f ->  f.getAddressee().getId().equals(currentUserId))
                .orElse(false);
    }

    public boolean isFriendOf(Authentication auth, Long userId) {
        Long currentUserId = userHolder.getCurrentUser(auth).getId();
        return userFriendshipRepository.findAcceptedFriendships(userId).stream()
                .anyMatch(f -> f.getRequester().getId().equals(currentUserId)
                        || f.getAddressee().getId().equals(currentUserId));
    }
}
