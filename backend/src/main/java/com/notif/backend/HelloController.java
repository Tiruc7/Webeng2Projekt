package com.notif.backend;

import helper.TMQueryBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {

    @GetMapping("/api/hello")
    public String hello() {
        return "Backend works. Yay.";
    }

    @Value("${ticketmaster.api.key}")
    private String ticketmasterApiKey;

    @GetMapping("/api/test-key")
    public String testKey() {
        if (ticketmasterApiKey == null || ticketmasterApiKey.isBlank()) {
            return "Key wurde nicht geladen";
        }
        return "Key wurde geladen. Länge: ";
    }

    @GetMapping("/api/hello")
    public String url() {
        TMQueryBuilder builder = new TMQueryBuilder();

        return builder.buildSearchUrl(
                "https://app.ticketmaster.com/discovery/v2",
                "MEIN_KEY",
                "metallica",
                "berlin",
                10
        );
    }




}