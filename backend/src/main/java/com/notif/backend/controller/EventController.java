package com.notif.backend.controller;

import com.notif.backend.dto.ConcertDTO;
import com.notif.backend.entity.Event;
import com.notif.backend.helper.EventService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/events")
public class EventController {

    private final EventService eventService;

    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

    @PostMapping("/sync")
    public Event syncEvent(@RequestBody ConcertDTO concertDTO) {
        return eventService.saveOrUpdateConcert(concertDTO);
    }

    @GetMapping
    public List<Event> getSavedEvents() {
        return eventService.getAllEvents();
    }


}