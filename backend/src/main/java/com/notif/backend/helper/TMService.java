package com.notif.backend.helper;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class TMService {

    @Value("${ticketmaster.api.base-url}")
    private String baseUrl;

    @Value("${ticketmaster.api.key}")
    private String apiKey;

    private final TMQueryBuilder tmQueryBuilder;

    private final RestTemplate restTemplate = new RestTemplate();

    public TMService(TMQueryBuilder tmQueryBuilder) {
        this.tmQueryBuilder = tmQueryBuilder;
    }

    public String getRawConcerts(String keyword, String city, int size) {
        String url = tmQueryBuilder.buildSearchUrl(baseUrl, apiKey, keyword, city, size);
        System.out.println("Ticketmaster URL: " + url);
        return restTemplate.getForObject(url, String.class);
    }
}