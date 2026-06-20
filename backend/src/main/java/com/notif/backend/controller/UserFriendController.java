package com.notif.backend.controller;

import com.notif.backend.Keycloak.KeycloakUserHolder;
import com.notif.backend.dto.UserFriendshipDTO;
import com.notif.backend.entity.User;
import com.notif.backend.service.FriendshipService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/friends")
@PreAuthorize("hasRole('USER')")
public class UserFriendController {

    private final FriendshipService friendshipService;
    private final KeycloakUserHolder userHolder;

    public UserFriendController(FriendshipService friendshipService,
                                KeycloakUserHolder userHolder) {
        this.friendshipService = friendshipService;
        this.userHolder = userHolder;
    }

    // Send a friend request to another user
    @PostMapping("/request/{addresseeId}")
    public ResponseEntity<Void> sendRequest(
            @PathVariable Long addresseeId,
            Authentication auth) throws Exception {
        User requester = userHolder.getCurrentUser(auth);
        friendshipService.sendRequest(requester.getId(), addresseeId);
        return ResponseEntity.ok().build();
    }

    // Accept or decline a pending request — only the addressee can respond
    @PutMapping("/{friendshipId}/respond")
    @PreAuthorize("@guard.isFriendshipAddressee(authentication, #friendshipId)")
    public ResponseEntity<Void> respondToRequest(
            @PathVariable Long friendshipId,
            @RequestParam boolean accept,
            Authentication auth) throws Exception {
        friendshipService.respondToRequest(friendshipId, accept);
        return ResponseEntity.ok().build();
    }

    // Get own accepted friends
    @GetMapping
    public ResponseEntity<List<UserFriendshipDTO>> getMyFriends(Authentication auth) {
        User user = userHolder.getCurrentUser(auth);
        return ResponseEntity.ok(friendshipService.getFriends(user.getId()));
    }

    // Get own pending incoming requests
    @GetMapping("/pending")
    public ResponseEntity<List<UserFriendshipDTO>> getPendingRequests(Authentication auth) {
        User user = userHolder.getCurrentUser(auth);
        return ResponseEntity.ok(friendshipService.getPendingRequests(user.getId()));
    }

    @DeleteMapping("/{friendshipId}")
    @PreAuthorize("hasRole('ADMIN') or @guard.isFriendshipParticipant(authentication, #friendshipId)")
    public ResponseEntity<Void> deleteFriendship(@PathVariable Long friendshipId) {
        friendshipService.deleteFriendship(friendshipId);
        return ResponseEntity.noContent().build();
    }
}