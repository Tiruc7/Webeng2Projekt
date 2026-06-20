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
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CommentService {

    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final UserEventRepository userEventRepository;

    public CommentService(
            CommentRepository commentRepository,
            UserRepository userRepository,
            EventRepository eventRepository,
            UserEventRepository userEventRepository
    ) {
        this.commentRepository = commentRepository;
        this.userRepository = userRepository;
        this.eventRepository = eventRepository;
        this.userEventRepository = userEventRepository;
    }

    public CommentDTO createComment(@NonNull Long userId, @NonNull String eventId, CreateCommentDTO dto) {
        //Nur kommentieren wenn user event gespeichert hat
        boolean userSavedEvent = userEventRepository.existsByUserIdAndEventId(userId, eventId);

        if (!userSavedEvent) {
            throw new RuntimeException("User is not allowed to comment on this event");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found"));

        Comment comment = new Comment();
        comment.setUser(user);
        comment.setEvent(event);
        comment.setContent(dto.content());

        Comment saved = commentRepository.save(comment);

        return toDto(saved);
    }

    public List<CommentDTO> getCommentsForEvent(String eventId) {
        return commentRepository.findByEventIdOrderByCreatedAtAsc(eventId)
                .stream()
                .map(this::toDto)
                .toList();
    }

    private CommentDTO toDto(Comment comment) {
        return new CommentDTO(
                comment.getId(),
                comment.getUser().getId(),
                comment.getUser().getUsername(),
                comment.getEvent().getId(),
                comment.getContent(),
                comment.getCreatedAt()
        );
    }
}