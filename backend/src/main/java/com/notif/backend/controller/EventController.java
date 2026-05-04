package com.notif.backend.controller;

import com.notif.backend.dto.EventDTO;
import com.notif.backend.entity.Event;
import com.notif.backend.helper.EventService;
import com.notif.backend.helper.TMService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class EventController {

    private final TMService tmService;
    private final EventService eventService;

    public EventController(TMService tmService, EventService eventService) {
        this.tmService = tmService;
        this.eventService = eventService;
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
        return tmService.getEvents(keyword, city, size);
    }
    @PostMapping("/sync")
    public Event syncEvent(@RequestBody EventDTO eventDTO) {
        return eventService.saveOrUpdateEvent(eventDTO);
    }

    @GetMapping
    public List<Event> getSavedEvents() {
        return eventService.getAllEvents();
    }

    @GetMapping("/api/events/sync")
    public List<Event> syncEventsFromTM(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String city,
            @RequestParam(defaultValue = "3") int size
    ) {
        List<EventDTO> events = tmService.getEvents(keyword, city, size);
        return eventService.saveOrUpdateEvent(events);
    }
}
