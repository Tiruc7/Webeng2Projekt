package com.notif.backend.service;

import com.notif.backend.dto.UserFriendshipDTO;
import com.notif.backend.entity.User;
import com.notif.backend.entity.UserFriendship;
import com.notif.backend.enums.FriendshipStatus;
import com.notif.backend.repository.UserFriendshipRepository;
import com.notif.backend.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.crossstore.ChangeSetPersister;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)

@SuppressWarnings("null")
class FriendshipServiceTest {

    @Mock UserFriendshipRepository userFriendshipRepository;
    @Mock UserRepository           userRepository;

    @InjectMocks FriendshipService service;

    // ── helpers ──────────────────────────────────────────────────────────────

    private User user(Long id, String name) {
        User u = new User();
        u.setId(id);
        u.setUserName(name);
        return u;
    }

    private UserFriendship friendship(Long id, User requester, User addressee, FriendshipStatus status) {
        UserFriendship f = new UserFriendship();
        f.setId(id);
        f.setRequester(requester);
        f.setAddressee(addressee);
        f.setStatus(status);
        return f;
    }

    // ── sendRequest ───────────────────────────────────────────────────────────

    @Test
    void sendRequest_savesNewFriendshipWithPendingStatus() throws Exception {
        User alice = user(1L, "alice");
        User bob   = user(2L, "bob");

        when(userFriendshipRepository.existsByUsers(1L, 2L)).thenReturn(false);
        when(userRepository.findById(1L)).thenReturn(Optional.of(alice));
        when(userRepository.findById(2L)).thenReturn(Optional.of(bob));

        service.sendRequest(1L, 2L);

        ArgumentCaptor<UserFriendship> captor = ArgumentCaptor.forClass(UserFriendship.class);
        verify(userFriendshipRepository).save(captor.capture());

        UserFriendship saved = captor.getValue();
        assertThat(saved.getRequester()).isEqualTo(alice);
        assertThat(saved.getAddressee()).isEqualTo(bob);
        assertThat(saved.getStatus()).isEqualTo(FriendshipStatus.PENDING);
    }

    @Test
    void sendRequest_throwsWhenRequesterAndAddresseeAreTheSame() {
        assertThatThrownBy(() -> service.sendRequest(1L, 1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Can't friend yourself");

        verifyNoInteractions(userFriendshipRepository, userRepository);
    }

    @Test
    void sendRequest_throwsWhenFriendshipAlreadyExists() {
        when(userFriendshipRepository.existsByUsers(1L, 2L)).thenReturn(true);

        assertThatThrownBy(() -> service.sendRequest(1L, 2L))
                .isInstanceOf(Exception.class)
                .hasMessageContaining("Friendship already exists");

        verify(userFriendshipRepository, never()).save(any());
    }

    @Test
    void sendRequest_throwsWhenRequesterNotFound() {
        when(userFriendshipRepository.existsByUsers(1L, 2L)).thenReturn(false);
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.sendRequest(1L, 2L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Requester not found");

        verify(userFriendshipRepository, never()).save(any());
    }

    @Test
    void sendRequest_throwsWhenAddresseeNotFound() {
        when(userFriendshipRepository.existsByUsers(1L, 2L)).thenReturn(false);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user(1L, "alice")));
        when(userRepository.findById(2L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.sendRequest(1L, 2L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Addressee not found");

        verify(userFriendshipRepository, never()).save(any());
    }

    // ── respondToRequest ──────────────────────────────────────────────────────

    @Test
    void respondToRequest_acceptSetsStatusAndSaves() throws Exception {
        UserFriendship pending = friendship(10L, user(1L, "alice"), user(2L, "bob"), FriendshipStatus.PENDING);
        when(userFriendshipRepository.findById(10L)).thenReturn(Optional.of(pending));

        service.respondToRequest(10L, true);

        assertThat(pending.getStatus()).isEqualTo(FriendshipStatus.ACCEPTED);
        verify(userFriendshipRepository).save(pending);
        verify(userFriendshipRepository, never()).delete(any());
    }

    @Test
    void respondToRequest_declineDeletesFriendship() throws Exception {
        UserFriendship pending = friendship(10L, user(1L, "alice"), user(2L, "bob"), FriendshipStatus.PENDING);
        when(userFriendshipRepository.findById(10L)).thenReturn(Optional.of(pending));

        service.respondToRequest(10L, false);

        verify(userFriendshipRepository).delete(pending);
        verify(userFriendshipRepository, never()).save(any());
    }

    @Test
    void respondToRequest_throwsWhenFriendshipNotFound() {
        when(userFriendshipRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.respondToRequest(99L, true))
                .isInstanceOf(ChangeSetPersister.NotFoundException.class);
    }

    // ── getFriends ────────────────────────────────────────────────────────────

    @Test
    void getFriends_returnsMappedDTOs() {
        User alice = user(1L, "alice");
        User bob   = user(2L, "bob");
        UserFriendship f = friendship(10L, alice, bob, FriendshipStatus.ACCEPTED);

        when(userFriendshipRepository.findAcceptedFriendships(1L)).thenReturn(List.of(f));

        List<UserFriendshipDTO> result = service.getFriends(1L);

        assertThat(result).hasSize(1);
        // UserFriendshipDTO::from is the real mapping; we just verify the pipeline ran
        verify(userFriendshipRepository).findAcceptedFriendships(1L);
    }

    @Test
    void getFriends_returnsEmptyListWhenNoFriends() {
        when(userFriendshipRepository.findAcceptedFriendships(1L)).thenReturn(List.of());

        assertThat(service.getFriends(1L)).isEmpty();
    }

    // ── getPendingRequests ────────────────────────────────────────────────────

    @Test
    void getPendingRequests_returnsMappedDTOs() {
        User alice = user(1L, "alice");
        User bob   = user(2L, "bob");
        UserFriendship f = friendship(10L, alice, bob, FriendshipStatus.PENDING);

        when(userFriendshipRepository.findPendingRequestsForAddressee(2L)).thenReturn(List.of(f));

        List<UserFriendshipDTO> result = service.getPendingRequests(2L);

        assertThat(result).hasSize(1);
        verify(userFriendshipRepository).findPendingRequestsForAddressee(2L);
    }

    @Test
    void getPendingRequests_returnsEmptyListWhenNoPending() {
        when(userFriendshipRepository.findPendingRequestsForAddressee(2L)).thenReturn(List.of());

        assertThat(service.getPendingRequests(2L)).isEmpty();
    }

    // ── deleteFriendship ──────────────────────────────────────────────────────

    @Test
    void deleteFriendship_deletesExistingFriendship() {
        UserFriendship f = friendship(10L, user(1L, "alice"), user(2L, "bob"), FriendshipStatus.ACCEPTED);
        when(userFriendshipRepository.findById(10L)).thenReturn(Optional.of(f));

        service.deleteFriendship(10L);

        verify(userFriendshipRepository).delete(f);
    }

    @Test
    void deleteFriendship_throwsWhenFriendshipNotFound() {
        when(userFriendshipRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.deleteFriendship(99L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Friendship not found");

        verify(userFriendshipRepository, never()).delete(any());
    }
}