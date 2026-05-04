package com.notif.backend.controller;

import com.notif.backend.dto.SearchSuggestionDTO;
import com.notif.backend.helper.TMService;
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

        return tmService.searchSuggestions(keyword.trim(), city, 5);
    }
}