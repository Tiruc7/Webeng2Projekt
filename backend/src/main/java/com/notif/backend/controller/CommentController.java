package com.notif.backend.controller;

import com.notif.backend.Keycloak.KeycloakUserHolder;
import com.notif.backend.dto.CommentDTO;
import com.notif.backend.dto.CreateCommentDTO;
import com.notif.backend.entity.User;
import com.notif.backend.service.CommentService;
import org.springframework.lang.NonNull;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/api/events/{eventId}/comments")
public class CommentController {

    private final CommentService commentService;
    private final KeycloakUserHolder userHolder;

    public CommentController(CommentService commentService, KeycloakUserHolder userHolder) {
        this.commentService = commentService;
        this.userHolder = userHolder;
    }

    @GetMapping
    public List<CommentDTO> getComments(@PathVariable String eventId) {
        return commentService.getCommentsForEvent(eventId);
    }

    @PostMapping
    @PreAuthorize("hasRole('USER')")
    public CommentDTO createComment(
            @PathVariable @NonNull String eventId,
            @RequestBody CreateCommentDTO dto,
            Authentication auth
    ) {
        User user = userHolder.getCurrentUser(auth);
        return commentService.createComment(Objects.requireNonNull(user.getId()), eventId, dto);
    }
}