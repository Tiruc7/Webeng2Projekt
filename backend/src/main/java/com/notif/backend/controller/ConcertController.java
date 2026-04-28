package com.notif.backend.controller;

import com.notif.backend.dto.ConcertDTO;
import com.notif.backend.entity.Event;
import com.notif.backend.helper.EventService;
import com.notif.backend.helper.TMService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class ConcertController {

    private final TMService tmService;
    private final EventService eventService;

    public ConcertController(TMService tmService, EventService eventService) {
        this.tmService = tmService;
        this.eventService = eventService;
    }

    @GetMapping("/api/concerts/raw")
    public String getRawConcerts(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String city,
            @RequestParam(defaultValue = "3") int size
    ) {
        return tmService.getRawConcerts(keyword, city, size);
    }

    //Zweiter Endpunkt locahost.../api/concerts
    @GetMapping("/api/concerts")
    public List<ConcertDTO> getConcerts(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String city,
            @RequestParam(defaultValue = "3") int size
    ) {
        return tmService.getConcerts(keyword, city, size);
    }
    @GetMapping("/api/concerts/sync")
    public List<Event> syncConcertsFromTicketmaster(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String city,
            @RequestParam(defaultValue = "3") int size
    ) {
        List<ConcertDTO> concerts = tmService.getConcerts(keyword, city, size);
        return eventService.saveOrUpdateConcerts(concerts);
    }

}
