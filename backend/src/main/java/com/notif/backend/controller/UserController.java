package com.notif.backend.controller;

import com.notif.backend.Keycloak.KeycloakUserHolder;
import com.notif.backend.dto.EventDTO;
import com.notif.backend.dto.UserDTO;
import com.notif.backend.entity.User;
import com.notif.backend.service.ICalService;
import com.notif.backend.service.UserEventService;
import com.notif.backend.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import java.nio.charset.StandardCharsets;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/user")
@PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
public class UserController {

    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    private final UserService userService;
    private final UserEventService userEventService;
    private final ICalService icalService;
    private final KeycloakUserHolder userHolder;

    public UserController(UserService userService, UserEventService userEventService, ICalService icalService, KeycloakUserHolder userHolder) {
        this.userService = userService;
        this.userEventService = userEventService;
        this.icalService = icalService;
        this.userHolder = userHolder;
    }

    @PutMapping("/{userId}")
    @PreAuthorize("@guard.isUser(authentication, #userId)")
    public ResponseEntity<UserDTO> updateUserData(
            @PathVariable Long userId,
            @RequestBody UserDTO userDto) {
        return ResponseEntity.ok(userService.updateUserData(userDto));
    }

    @DeleteMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN') or @guard.isUser(authentication, #userId)")
    public ResponseEntity<Void> deleteUser(@PathVariable Long userId) {
        userService.deleteUserData(userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{userId}/events")
    public ResponseEntity<List<EventDTO>> getEventsForUser(
            @PathVariable Long userId,
            @AuthenticationPrincipal Jwt jwt) {
        UserDTO requester = userService.getOrCreateUser(jwt);
        if (!requester.id().equals(userId)) {
            log.warn("Access denied: user {} tried to access events of user {}", requester.id(), userId);
            return ResponseEntity.status(403).build();
        }
        return ResponseEntity.ok(userEventService.getEventsForUser(userId));
    }

    @PostMapping("/events")
    public ResponseEntity<Void> saveEvent(
            Authentication auth,
            @RequestBody EventDTO eventDTO) {
        User user = userHolder.getCurrentUser(auth);
        userEventService.addEventToUserProfile(user.getId(), eventDTO);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/events/{eventId}")
    public ResponseEntity<Void> deleteEvent(@PathVariable String eventId, Authentication auth) {
        UserDTO user = userHolder.getCurrentUser(auth).toDTO();
        userEventService.removeEventFromUserProfile(user.id(), eventId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/events")
    public ResponseEntity<List<EventDTO>> getMyEvents(Authentication auth) {
        User user = userHolder.getCurrentUser(auth);
        return ResponseEntity.ok(userEventService.getEventsForUser(user.getId()));
    }

    @GetMapping("/events/export/ical")
    public ResponseEntity<byte[]> exportEventsAsIcal(Authentication auth) {
        UserDTO user = userHolder.getCurrentUser(auth).toDTO();
        String icalContent = icalService.generate(userEventService.getEventsForUser(user.id()));

        byte[] bytes = icalContent.getBytes(StandardCharsets.UTF_8);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("text/calendar; charset=utf-8"));
        headers.setContentDispositionFormData("attachment", "concerts.ics");
        headers.setContentLength(bytes.length);

        return ResponseEntity.ok()
                .headers(headers)
                .body(bytes);
    }

    @GetMapping("/{userId}/profile")
    @PreAuthorize("hasRole('ADMIN') or @guard.isUser(authentication, #userId)")
    public ResponseEntity<UserDTO> getUserProfile(@PathVariable Long userId) {
        return ResponseEntity.ok(userService.getUserByID(userId));
    }

    // Bootstrap endpoint. All other endpoints use userHolder.getCurrentUser(auth) instead.
    @GetMapping("/sync")
    public ResponseEntity<UserDTO> syncLogin(@AuthenticationPrincipal Jwt jwt) {
        UserDTO user = userService.getOrCreateUser(jwt);
        log.info("User logged in: userId={}", user.id());
        return ResponseEntity.ok(user);
    }

}
