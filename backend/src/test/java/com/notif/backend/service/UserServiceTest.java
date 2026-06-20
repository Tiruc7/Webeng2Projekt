package com.notif.backend.service;

import com.notif.backend.dto.UserDTO;
import com.notif.backend.entity.User;
import com.notif.backend.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)

@SuppressWarnings("null")
class UserServiceTest {

    @Mock UserRepository userRepository;

    @InjectMocks UserService service;

    private Jwt jwt(String sub, String username) {
        Jwt jwt = mock(Jwt.class);
        when(jwt.getClaimAsString("sub")).thenReturn(sub);
        when(jwt.getClaimAsString("preferred_username")).thenReturn(username);
        return jwt;
    }

    private User existingUser() {
        User u = new User();
        u.setId(3L);
        u.setExternalId("ext-123");
        u.setUserName("alice");
        return u;
    }

    @Test
    void getOrCreateUser_returnsExistingUser() {
        // Arrange — user is already in the DB
        when(userRepository.findByExternalId("ext-123"))
                .thenReturn(Optional.of(existingUser()));

        // Act
        UserDTO result = service.getOrCreateUser(jwt("ext-123", "alice"));

        // Assert — existing user is returned without creating a new one
        assertThat(result.username()).isEqualTo("alice");
        assertThat(result.id()).isEqualTo(3L);
        verify(userRepository, never()).save(any());
    }

    @Test
    void getOrCreateUser_createsUserWhenMissing() {
        // Arrange — first login, user does not exist yet (JIT provisioning)
        when(userRepository.findByExternalId("ext-123"))
                .thenReturn(Optional.empty());
        when(userRepository.save(any(User.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        // Act
        UserDTO result = service.getOrCreateUser(jwt("ext-123", "alice"));

        // Assert — new user is persisted and DTO carries the correct username
        verify(userRepository).save(any(User.class));
        assertThat(result.username()).isEqualTo("alice");
    }
}
