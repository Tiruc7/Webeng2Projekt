package com.notif.backend.controller;

import com.notif.backend.helper.TMService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ConcertController {

    private final TMService tmService;

    public ConcertController(TMService tmService) {
        this.tmService = tmService;
    }

    @GetMapping("/api/concerts/raw")
    public String getRawConcerts(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String city,
            @RequestParam(defaultValue = "3") int size
    ) {
        return tmService.getRawConcerts(keyword, city, size);
    }

}
