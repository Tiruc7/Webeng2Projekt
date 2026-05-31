package com.notif.backend.controller;

import com.notif.backend.dto.CommentDTO;
import com.notif.backend.dto.CreateCommentDTO;
import com.notif.backend.service.CommentService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/events/{eventId}/comments")
public class CommentController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @GetMapping
    public List<CommentDTO> getComments(@PathVariable String eventId) {
        return commentService.getCommentsForEvent(eventId);
    }

    @PostMapping
    public CommentDTO createComment(
            @PathVariable String eventId,
            @RequestParam Long userId,
            @RequestBody CreateCommentDTO dto
    ) {
        return commentService.createComment(userId, eventId, dto);
    }
}