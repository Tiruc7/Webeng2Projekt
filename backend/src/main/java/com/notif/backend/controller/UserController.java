package com.notif.backend.controller;

import com.notif.backend.dto.EventDTO;
import com.notif.backend.dto.UserDTO;
import com.notif.backend.service.UserEventService;
import com.notif.backend.service.UserService;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/user")
@PreAuthorize("hasRole('USER')")
public class UserController {

    private final UserService userService;
    private final UserEventService userEventService;

    public UserController(UserService userService, UserEventService userEventService){
        this.userService = userService;
        this.userEventService = userEventService;
    }

    @PutMapping()
    public ResponseEntity<UserDTO> updateUserData(@RequestBody UserDTO userDto) {
        try {
            UserDTO updated = userService.updateUserData(userDto);
            return ResponseEntity.ok(updated); //Status 200
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    @DeleteMapping()
    public ResponseEntity updateUserData(@RequestParam(value = "userId") Long userId) {
        try {
            userService.deleteUserData(userId);
            return new ResponseEntity<>(HttpStatusCode.valueOf(200));
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatusCode.valueOf(500));
        }
    }

    @GetMapping("/{userId}/events")
    public List<EventDTO> getEventsForUser(@PathVariable Long userId) {
        return userEventService.getEventsForUser(userId);
    }

    @PostMapping("/events")
    public ResponseEntity<Void> saveEvent(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody EventDTO eventDTO
    ) {
        UserDTO user = userService.getOrCreateUser(jwt);
        userEventService.addEventToUserProfile(user.id(), eventDTO);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/events/{eventId}")
    public ResponseEntity<Void> deleteEvent(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable String eventId
    ) {
        UserDTO user = userService.getOrCreateUser(jwt);
        userEventService.removeEventFromUserProfile(user.id(), eventId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/events")
    public List<EventDTO> getMyEvents(@AuthenticationPrincipal Jwt jwt) {
        UserDTO user = userService.getOrCreateUser(jwt);
        return userEventService.getEventsForUser(user.id());
    }

    @GetMapping("/{userId}/profile")
    public UserDTO getUserProfile(@PathVariable Long userId) {
        return userService.getUserByID(userId);
    }

    @GetMapping("/sync")
    public ResponseEntity<UserDTO> syncLogin(@AuthenticationPrincipal Jwt jwt) {
        // Ruft den Service auf, der getOrCreateUser implementiert
        UserDTO user = userService.getOrCreateUser(jwt);
        return ResponseEntity.ok(user);
    }

}
