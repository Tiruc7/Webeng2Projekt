package com.notif.backend.controller;

import com.notif.backend.dto.EventDTO;
import com.notif.backend.dto.UserDTO;
import com.notif.backend.service.UserEventService;
import com.notif.backend.service.UserService;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class UserController {

    private final UserService userService;
    private final UserEventService userEventService;

    public UserController(UserService userService, UserEventService userEventService){
        this.userService = userService;
        this.userEventService = userEventService;
    }

    @PutMapping(value = "/user")
    public ResponseEntity<UserDTO> updateUserData(@RequestBody UserDTO userDto) {
        try {
            UserDTO updated = userService.updateUserData(userDto);
            return ResponseEntity.ok(updated); //Status 200
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    @DeleteMapping(value = "/user")
    public ResponseEntity updateUserData(@RequestParam(value = "userId") Long userId) {
        try {
            userService.deleteUserData(userId);
            return new ResponseEntity<>(HttpStatusCode.valueOf(200));
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatusCode.valueOf(500));
        }
    }

    @GetMapping("/{userId}/events")
    @PreAuthorize("hasRole('USER')")
    public List<EventDTO> getEventsForUser(@PathVariable Long userId) {
        return userEventService.getEventsForUser(userId);
    }

}
