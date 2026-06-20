package com.notif.backend.service;

import com.notif.backend.dto.CommentDTO;
import com.notif.backend.dto.CreateCommentDTO;
import com.notif.backend.entity.Comment;
import com.notif.backend.entity.Event;
import com.notif.backend.entity.User;
import com.notif.backend.repository.CommentRepository;
import com.notif.backend.repository.EventRepository;
import com.notif.backend.repository.UserEventRepository;
import com.notif.backend.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)

@SuppressWarnings("null")
class CommentServiceTest {

    @Mock CommentRepository   commentRepository;
    @Mock UserRepository      userRepository;
    @Mock EventRepository     eventRepository;
    @Mock UserEventRepository userEventRepository;

    @InjectMocks CommentService service;

    // ── helpers ───────────────────────────────────────────────────────────────

    private User user(Long id, String username) {
        User u = new User();
        u.setId(id);
        u.setUserName(username);
        return u;
    }

    private Event event(String id) {
        Event e = new Event();
        e.setId(id);
        return e;
    }

    private Comment comment(Long id, User user, Event event, String content) {
        Comment c = new Comment();
        c.setId(id);
        c.setUser(user);
        c.setEvent(event);
        c.setContent(content);
        c.setCreatedAt(LocalDateTime.of(2025, 1, 1, 12, 0));
        return c;
    }

    // ── createComment ─────────────────────────────────────────────────────────

    @Test
    void createComment_savesCommentAndReturnsDTO() {
        User alice = user(1L, "alice");
        Event ev   = event("evt-42");

        when(userEventRepository.existsByUserIdAndEventId(1L, "evt-42")).thenReturn(true);
        when(userRepository.findById(1L)).thenReturn(Optional.of(alice));
        when(eventRepository.findById("evt-42")).thenReturn(Optional.of(ev));
        when(commentRepository.save(any(Comment.class)))
                .thenAnswer(inv -> {
                    Comment c = inv.getArgument(0);
                    c.setId(99L);
                    c.setCreatedAt(LocalDateTime.of(2025, 1, 1, 12, 0));
                    return c;
                });

        CommentDTO result = service.createComment(1L, "evt-42", new CreateCommentDTO("Nice event!"));

        assertThat(result.id()).isEqualTo(99L);
        assertThat(result.userId()).isEqualTo(1L);
        assertThat(result.username()).isEqualTo("alice");
        assertThat(result.eventId()).isEqualTo("evt-42");
        assertThat(result.content()).isEqualTo("Nice event!");
    }

    @Test
    void createComment_persistsCorrectFields() {
        User alice = user(1L, "alice");
        Event ev   = event("evt-42");

        when(userEventRepository.existsByUserIdAndEventId(1L, "evt-42")).thenReturn(true);
        when(userRepository.findById(1L)).thenReturn(Optional.of(alice));
        when(eventRepository.findById("evt-42")).thenReturn(Optional.of(ev));
        when(commentRepository.save(any(Comment.class))).thenAnswer(inv -> {
            Comment c = inv.getArgument(0);
            c.setId(1L);
            c.setCreatedAt(LocalDateTime.now());
            return c;
        });

        service.createComment(1L, "evt-42", new CreateCommentDTO("Nice event!"));

        ArgumentCaptor<Comment> captor = ArgumentCaptor.forClass(Comment.class);
        verify(commentRepository).save(captor.capture());

        Comment saved = captor.getValue();
        assertThat(saved.getUser()).isEqualTo(alice);
        assertThat(saved.getEvent()).isEqualTo(ev);
        assertThat(saved.getContent()).isEqualTo("Nice event!");
    }

    @Test
    void createComment_throwsWhenUserHasNotSavedEvent() {
        when(userEventRepository.existsByUserIdAndEventId(1L, "evt-42")).thenReturn(false);

        assertThatThrownBy(() -> service.createComment(1L, "evt-42", new CreateCommentDTO("hello")))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("User is not allowed to comment on this event");

        verifyNoInteractions(userRepository, eventRepository, commentRepository);
    }

    @Test
    void createComment_throwsWhenUserNotFound() {
        when(userEventRepository.existsByUserIdAndEventId(1L, "evt-42")).thenReturn(true);
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.createComment(1L, "evt-42", new CreateCommentDTO("hello")))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("User not found");

        verify(commentRepository, never()).save(any());
    }

    @Test
    void createComment_throwsWhenEventNotFound() {
        when(userEventRepository.existsByUserIdAndEventId(1L, "evt-42")).thenReturn(true);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user(1L, "alice")));
        when(eventRepository.findById("evt-42")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.createComment(1L, "evt-42", new CreateCommentDTO("hello")))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Event not found");

        verify(commentRepository, never()).save(any());
    }

    // ── getCommentsForEvent ───────────────────────────────────────────────────

    @Test
    void getCommentsForEvent_returnsMappedDTOsInOrder() {
        User alice = user(1L, "alice");
        Event ev   = event("evt-42");
        Comment c1 = comment(1L, alice, ev, "First!");
        Comment c2 = comment(2L, alice, ev, "Second!");

        when(commentRepository.findByEventIdOrderByCreatedAtAsc("evt-42"))
                .thenReturn(List.of(c1, c2));

        List<CommentDTO> result = service.getCommentsForEvent("evt-42");

        assertThat(result).hasSize(2);
        assertThat(result.get(0).content()).isEqualTo("First!");
        assertThat(result.get(1).content()).isEqualTo("Second!");
    }

    @Test
    void getCommentsForEvent_returnsEmptyListWhenNoComments() {
        when(commentRepository.findByEventIdOrderByCreatedAtAsc("evt-42"))
                .thenReturn(List.of());

        assertThat(service.getCommentsForEvent("evt-42")).isEmpty();
    }
}