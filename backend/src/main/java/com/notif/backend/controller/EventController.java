package com.notif.backend.controller;

import com.notif.backend.dto.EventDTO;
import com.notif.backend.dto.UserDTO;
import com.notif.backend.entity.Event;
import com.notif.backend.service.EventService;
import com.notif.backend.service.TMService;
import com.notif.backend.service.UserEventService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class EventController {

    private final TMService tmService;
    private final EventService eventService;
    private final UserEventService userEventService;

    public EventController(TMService tmService, EventService eventService, UserEventService userEventService) {
        this.tmService = tmService;
        this.eventService = eventService;
        this.userEventService = userEventService;
    }

    @GetMapping("/api/events/raw")
    public String getRawEvents(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String city,
            @RequestParam(defaultValue = "3") int size
    ) {
        return tmService.getRawEvents(keyword, city, size);
    }

    @GetMapping("/api/events")
    public List<EventDTO> getEvents(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String city,
            @RequestParam(defaultValue = "3") int size
    ) {
        return tmService.getEvents(keyword, city, size, null, null);
    }
    @PostMapping("/sync")
    public Event syncEvent(@RequestBody EventDTO eventDTO) {
        return eventService.saveOrUpdateEvent(eventDTO);
    }

    @GetMapping
    public List<EventDTO> getSavedEvents() {
        return eventService.getAllEvents();
    }

    @GetMapping("/api/events/sync")
    public List<Event> syncEventsFromTM(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String city,
            @RequestParam(defaultValue = "3") int size
    ) {
        List<EventDTO> events = tmService.getEvents(keyword, city, size, null, null);
        return eventService.saveOrUpdateEvent(events);
    }

    @GetMapping("/{eventId}/users")
    @PreAuthorize("hasRole('ADMIN')")
    public List<UserDTO> getEventsForUser(@PathVariable String eventId) {
        return userEventService.getUserForEvents(eventId);
    }
}
