package com.notif.backend.controller;

import com.notif.backend.dto.EventDTO;
import com.notif.backend.dto.UserDTO;
import com.notif.backend.dto.UserEventDTO;
import com.notif.backend.service.EventService;
import com.notif.backend.service.UserEventService;
import com.notif.backend.service.UserService;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("!hasRole('USER')")
public class AdminDataController {

    private final UserEventService userEventService;
    private final EventService eventService;
    private final UserService userService;

    public AdminDataController(UserEventService userEventService, EventService eventService, UserService userService) {

        this.userEventService = userEventService;
        this.eventService = eventService;
        this.userService = userService;
    }

    @GetMapping(value = "/users")
    public ResponseEntity<List<UserDTO>> getUserData() {
        try {
            return new ResponseEntity<>(userService.getUserData(), HttpStatusCode.valueOf(200));
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatusCode.valueOf(500));
        }
    }

    @GetMapping("/user-events")
    public List<UserEventDTO> getUserEvents() {
        return userEventService.getUserEventsByUser();
    }

    @GetMapping("/events")
    public List<EventDTO> getSavedEvents() {
        return eventService.getAllEvents();
    }
}