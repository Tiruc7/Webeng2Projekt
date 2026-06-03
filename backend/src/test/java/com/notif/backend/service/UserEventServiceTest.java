package com.notif.backend.service;

import com.notif.backend.dto.EventDTO;
import com.notif.backend.entity.Event;
import com.notif.backend.entity.User;
import com.notif.backend.entity.UserEvent;
import com.notif.backend.repository.EventRepository;
import com.notif.backend.repository.UserEventRepository;
import com.notif.backend.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserEventServiceTest {

    @Mock UserEventRepository userEventRepository;
    @Mock UserRepository      userRepository;
    @Mock EventRepository     eventRepository;

    @InjectMocks UserEventService service;

    private EventDTO dto(String id) {
        return new EventDTO(id, "Metallica Live", "Olympiastadion", "Berlin",
                "2026-08-14", "20:00", "", "", "onsale");
    }

    private User user(Long id) {
        User u = new User();
        u.setId(id);
        u.setUserName("alice");
        u.setExternalId("ext-1");
        return u;
    }

    private Event event(String id) {
        Event e = new Event();
        e.setId(id);
        return e;
    }

    // --- addEventToUserProfile ---

    @Test
    void addEvent_savesConnectionWhenNew() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(user(1L)));
        when(eventRepository.findById("evt1")).thenReturn(Optional.empty());
        when(eventRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(userEventRepository.existsByUser_IdAndEvent_Id(1L, "evt1")).thenReturn(false);

        // Act
        service.addEventToUserProfile(1L, dto("evt1"));

        // Assert — new user-event connection must be persisted
        verify(userEventRepository).save(any(UserEvent.class));
    }

    @Test
    void addEvent_skipsWhenAlreadySaved() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(user(1L)));
        when(eventRepository.findById("evt1")).thenReturn(Optional.of(event("evt1")));
        when(eventRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(userEventRepository.existsByUser_IdAndEvent_Id(1L, "evt1")).thenReturn(true);

        // Act
        service.addEventToUserProfile(1L, dto("evt1"));

        // Assert — duplicate must be silently ignored
        verify(userEventRepository, never()).save(any(UserEvent.class));
    }

    @Test
    void addEvent_throwsWhenUserNotFound() {
        // Arrange
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        // Act + Assert
        assertThatThrownBy(() -> service.addEventToUserProfile(99L, dto("evt1")))
                .isInstanceOf(EntityNotFoundException.class);
    }

    // --- removeEventFromUserProfile ---

    @Test
    void removeEvent_deletesOrphanEvent() {
        // Arrange — after removing the user's link, no other owner remains
        when(userEventRepository.existsByEvent_Id("evt1")).thenReturn(false);

        // Act
        service.removeEventFromUserProfile(1L, "evt1");

        // Assert — orphaned event must be deleted from the events table
        verify(eventRepository).deleteById("evt1");
    }

    @Test
    void removeEvent_keepsEventWithOtherOwners() {
        // Arrange — another user still has this event saved
        when(userEventRepository.existsByEvent_Id("evt1")).thenReturn(true);

        // Act
        service.removeEventFromUserProfile(1L, "evt1");

        // Assert — event must NOT be deleted
        verify(eventRepository, never()).deleteById(any());
    }
}
