package com.notif.backend.Keycloak;

import com.notif.backend.repository.UserEventRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component("guard")
public class OwnershipGuard {

    private final KeycloakUserHolder userHolder;
    private final UserEventRepository userEventRepo;

    public OwnershipGuard(KeycloakUserHolder userHolder, UserEventRepository userEventRepo) {
        this.userHolder = userHolder;
        this.userEventRepo = userEventRepo;
    }

    public boolean isUser(Authentication auth, Long userId) {
        return userHolder.getCurrentUser(auth).getId().equals(userId);
    }

    public boolean ownsUserEvent(Authentication auth, String userEventId) {
        return userEventRepo.findById(userEventId)
                .map(ue -> {
                    Long currentUserId = userHolder.getCurrentUser(auth).getId();
                    return ue.getUser().getId().equals(currentUserId);
                })
                .orElse(false);
    }
}
