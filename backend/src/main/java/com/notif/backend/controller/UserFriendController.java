package com.notif.backend.controller;


import com.notif.backend.dto.UserDTO;
import com.notif.backend.service.FriendshipService;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
@PreAuthorize("hasRole('USER')")
public class UserFriendController {
    private final FriendshipService friendshipService;

    public UserFriendController(FriendshipService friendshipService) {
        this.friendshipService = friendshipService;
    }

    @DeleteMapping()
    public ResponseEntity DeleteUserFriendship(@RequestParam(value = "userId") Long userId) {
        try {
            userService.deleteUserData(userId);
            return new ResponseEntity<>(HttpStatusCode.valueOf(200));
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatusCode.valueOf(500));
        }
    }

    @PutMapping()
    public ResponseEntity updateUserData(@RequestParam(value = "userId") Long userId) {
        try {
            userService.deleteUserData(userId);
            return new ResponseEntity<>(HttpStatusCode.valueOf(200));
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatusCode.valueOf(500));
        }
    }
}
