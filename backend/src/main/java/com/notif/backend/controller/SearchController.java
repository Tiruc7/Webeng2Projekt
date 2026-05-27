package com.notif.backend.controller;

import com.notif.backend.dto.EventDTO;
import com.notif.backend.dto.SearchSuggestionDTO;
import com.notif.backend.service.TMService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/api/search")
public class SearchController {

    private final TMService tmService;

    public SearchController(TMService tmService) {
        this.tmService = tmService;
    }

    @GetMapping("/suggestions")
    public List<SearchSuggestionDTO> getSuggestions(
            @RequestParam String keyword,
            @RequestParam(required = false) String city
    ) {
        if (keyword == null || keyword.trim().length() < 3) {
            return Collections.emptyList();
        }
        // Normalize: lowercase keyword + null for blank city -> consistent cache keys
        String normalizedKeyword = keyword.trim().toLowerCase();
        String normalizedCity = (city != null && !city.isBlank()) ? city.trim() : null;

        return tmService.searchSuggestions(normalizedKeyword, normalizedCity, 3);
    }

    @GetMapping("/events")
    public List<EventDTO> searchEvents(
            @RequestParam String keyword,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String dateFrom,
            @RequestParam(required = false) String dateTo,
            @RequestParam(defaultValue = "20") int size
    ) {
        if (keyword == null || keyword.trim().length() < 3) {
            return Collections.emptyList();
        }
        String normalizedKeyword = keyword.trim().toLowerCase();
        String normalizedCity = (city != null && !city.isBlank()) ? city.trim() : null;

        return tmService.getEvents(normalizedKeyword, normalizedCity, size, dateFrom, dateTo);
    }
}