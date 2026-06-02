package com.notif.backend.Keycloak;

import com.notif.backend.entity.User;
import com.notif.backend.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

@Component
public class KeycloakUserHolder{

    private final UserRepository userRepo;

    public KeycloakUserHolder(UserRepository userRepo) {
        this.userRepo = userRepo;
    }

    public String getCurrentKeycloakId(Authentication authentication) {
        Jwt jwt = (Jwt) authentication.getPrincipal();
        return jwt.getSubject(); // this is the "sub" claim = your external_id
    }

    public User getCurrentUser(Authentication authentication) {
        String keycloakId = getCurrentKeycloakId(authentication);
        return userRepo.findByExternalId(keycloakId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
    }
}