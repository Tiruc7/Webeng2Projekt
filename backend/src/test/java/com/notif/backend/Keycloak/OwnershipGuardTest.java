package com.notif.backend.Keycloak;

import com.notif.backend.entity.User;
import com.notif.backend.entity.UserEvent;
import com.notif.backend.entity.UserFriendship;
import com.notif.backend.enums.FriendshipStatus;
import com.notif.backend.repository.UserEventRepository;
import com.notif.backend.repository.UserFriendshipRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OwnershipGuardTest {

    @Mock KeycloakUserHolder       userHolder;
    @Mock UserEventRepository      userEventRepo;
    @Mock UserFriendshipRepository userFriendshipRepository;

    @InjectMocks OwnershipGuard guard;

    @Mock Authentication auth;

    // ── helpers ───────────────────────────────────────────────────────────────

    private User user(Long id) {
        User u = new User();
        u.setId(id);
        return u;
    }

    private UserEvent userEvent(String id, User owner) {
        UserEvent ue = new UserEvent();
        ue.setId(id);
        ue.setUser(owner);
        return ue;
    }

    private UserFriendship friendship(Long id, User requester, User addressee) {
        UserFriendship f = new UserFriendship();
        f.setId(id);
        f.setRequester(requester);
        f.setAddressee(addressee);
        f.setStatus(FriendshipStatus.ACCEPTED);
        return f;
    }

    /** Stubs userHolder to return a user with the given id for any auth token. */
    private void currentUserIs(Long id) {
        when(userHolder.getCurrentUser(auth)).thenReturn(user(id));
    }

    // ── isUser ────────────────────────────────────────────────────────────────

    @Test
    void isUser_returnsTrueWhenIdsMatch() {
        currentUserIs(1L);
        assertThat(guard.isUser(auth, 1L)).isTrue();
    }

    @Test
    void isUser_returnsFalseWhenIdsDiffer() {
        currentUserIs(1L);
        assertThat(guard.isUser(auth, 2L)).isFalse();
    }

    // ── ownsUserEvent ─────────────────────────────────────────────────────────

    @Test
    void ownsUserEvent_returnsTrueWhenCurrentUserOwnsEvent() {
        currentUserIs(1L);
        when(userEventRepo.findById("ue-1")).thenReturn(Optional.of(userEvent("ue-1", user(1L))));

        assertThat(guard.ownsUserEvent(auth, "ue-1")).isTrue();
    }

    @Test
    void ownsUserEvent_returnsFalseWhenDifferentUserOwnsEvent() {
        currentUserIs(1L);
        when(userEventRepo.findById("ue-1")).thenReturn(Optional.of(userEvent("ue-1", user(2L))));

        assertThat(guard.ownsUserEvent(auth, "ue-1")).isFalse();
    }

    @Test
    void ownsUserEvent_returnsFalseWhenUserEventNotFound() {
        when(userEventRepo.findById("ue-missing")).thenReturn(Optional.empty());

        assertThat(guard.ownsUserEvent(auth, "ue-missing")).isFalse();
    }

    // ── isFriendshipParticipant ───────────────────────────────────────────────

    @Test
    void isFriendshipParticipant_returnsTrueWhenCurrentUserIsRequester() {
        currentUserIs(1L);
        when(userFriendshipRepository.findById(10L))
                .thenReturn(Optional.of(friendship(10L, user(1L), user(2L))));

        assertThat(guard.isFriendshipParticipant(auth, 10L)).isTrue();
    }

    @Test
    void isFriendshipParticipant_returnsTrueWhenCurrentUserIsAddressee() {
        currentUserIs(2L);
        when(userFriendshipRepository.findById(10L))
                .thenReturn(Optional.of(friendship(10L, user(1L), user(2L))));

        assertThat(guard.isFriendshipParticipant(auth, 10L)).isTrue();
    }

    @Test
    void isFriendshipParticipant_returnsFalseWhenCurrentUserIsNotInFriendship() {
        currentUserIs(3L);
        when(userFriendshipRepository.findById(10L))
                .thenReturn(Optional.of(friendship(10L, user(1L), user(2L))));

        assertThat(guard.isFriendshipParticipant(auth, 10L)).isFalse();
    }

    @Test
    void isFriendshipParticipant_returnsFalseWhenFriendshipNotFound() {
        currentUserIs(1L);
        when(userFriendshipRepository.findById(99L)).thenReturn(Optional.empty());

        assertThat(guard.isFriendshipParticipant(auth, 99L)).isFalse();
    }

    // ── isFriendshipAddressee ─────────────────────────────────────────────────

    @Test
    void isFriendshipAddressee_returnsTrueWhenCurrentUserIsAddressee() {
        currentUserIs(2L);
        when(userFriendshipRepository.findById(10L))
                .thenReturn(Optional.of(friendship(10L, user(1L), user(2L))));

        assertThat(guard.isFriendshipAddressee(auth, 10L)).isTrue();
    }

    @Test
    void isFriendshipAddressee_returnsFalseWhenCurrentUserIsRequester() {
        currentUserIs(1L);
        when(userFriendshipRepository.findById(10L))
                .thenReturn(Optional.of(friendship(10L, user(1L), user(2L))));

        assertThat(guard.isFriendshipAddressee(auth, 10L)).isFalse();
    }

    @Test
    void isFriendshipAddressee_returnsFalseWhenFriendshipNotFound() {
        currentUserIs(1L);
        when(userFriendshipRepository.findById(99L)).thenReturn(Optional.empty());

        assertThat(guard.isFriendshipAddressee(auth, 99L)).isFalse();
    }

    // ── isFriendOf ────────────────────────────────────────────────────────────

    @Test
    void isFriendOf_returnsTrueWhenCurrentUserIsRequesterInAcceptedFriendship() {
        currentUserIs(1L);
        when(userFriendshipRepository.findAcceptedFriendships(2L))
                .thenReturn(List.of(friendship(10L, user(1L), user(2L))));

        assertThat(guard.isFriendOf(auth, 2L)).isTrue();
    }

    @Test
    void isFriendOf_returnsTrueWhenCurrentUserIsAddresseeInAcceptedFriendship() {
        currentUserIs(2L);
        when(userFriendshipRepository.findAcceptedFriendships(1L))
                .thenReturn(List.of(friendship(10L, user(3L), user(2L))));

        assertThat(guard.isFriendOf(auth, 1L)).isTrue();
    }

    @Test
    void isFriendOf_returnsFalseWhenNoAcceptedFriendshipExists() {
        currentUserIs(1L);
        when(userFriendshipRepository.findAcceptedFriendships(2L)).thenReturn(List.of());

        assertThat(guard.isFriendOf(auth, 2L)).isFalse();
    }

    @Test
    void isFriendOf_returnsFalseWhenCurrentUserNotInAnyAcceptedFriendship() {
        currentUserIs(99L);
        when(userFriendshipRepository.findAcceptedFriendships(2L))
                .thenReturn(List.of(friendship(10L, user(1L), user(2L))));

        assertThat(guard.isFriendOf(auth, 2L)).isFalse();
    }
}