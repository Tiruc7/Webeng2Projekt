package com.notif.backend.controller;


import com.notif.backend.dto.UserDTO;
import com.notif.backend.service.FriendshipService;
import com.notif.backend.service.UserService;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
@PreAuthorize("hasRole('USER')")
public class UserFriendController {
    private final FriendshipService friendshipService;
    private final UserService userService;

    public UserFriendController(FriendshipService friendshipService, UserService userService) {
        this.friendshipService = friendshipService;
        this.userService = userService;
    }

    @DeleteMapping()
    public ResponseEntity DeleteUserFriendship(@RequestParam(value = "userId") Long userId) {

    }

    @PutMapping()
    public ResponseEntity updateUserData(@RequestParam(value = "userId") Long userId) {

    }
}
