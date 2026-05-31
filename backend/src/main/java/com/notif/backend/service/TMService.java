package com.notif.backend.service;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.notif.backend.dto.EventDTO;

import java.util.ArrayList;
import java.util.List;

import com.notif.backend.dto.SearchSuggestionDTO;
import com.notif.backend.helper.TMQueryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class TMService {

    private static final Logger log = LoggerFactory.getLogger(TMService.class);

    @Value("${ticketmaster.api.base-url}")
    private String baseUrl;

    @Value("${ticketmaster.api.key}")
    private String apiKey;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final TMQueryBuilder tmQueryBuilder;

    private final RestTemplate restTemplate;

    @Autowired @Lazy
    private TMService self;

    public TMService(TMQueryBuilder tmQueryBuilder, RestTemplate restTemplate) {
        this.tmQueryBuilder = tmQueryBuilder;
        this.restTemplate = restTemplate;
    }

    public String getRawEvents(String keyword, String city, int size) {
        log.debug("Fetching raw events: keyword={}, city={}, size={}", keyword, city, size);
        String url = tmQueryBuilder.buildSearchUrl(baseUrl, apiKey, keyword, city, size);
        return restTemplate.getForObject(url, String.class);
    }

    @Cacheable("events")
    public List<EventDTO> getEvents(String keyword, String city, int size, String dateFrom, String dateTo) {
        log.info("Ticketmaster API call: keyword={}, city={}, size={}, dateFrom={}, dateTo={}", keyword, city, size, dateFrom, dateTo);
        String url = tmQueryBuilder.buildSearchUrl(baseUrl, apiKey, keyword, city, size, dateFrom, dateTo);
        String response = restTemplate.getForObject(url, String.class);

        List<EventDTO> concerts = new ArrayList<>();

        try {
            JsonNode root = objectMapper.readTree(response);
            JsonNode events = root.path("_embedded").path("events");

            if (events.isArray()) {
                for (JsonNode event : events) {
                    String id = event.path("id").asText("");
                    String title = event.path("name").asText("");
                    String date = event.path("dates").path("start").path("localDate").asText("");
                    String time = event.path("dates").path("start").path("localTime").asText("");


                    String imageUrl = "";
                    JsonNode images = event.path("images");
                    if (images.isArray() && !images.isEmpty()) {
                        imageUrl = images.get(0).path("url").asText("");
                    }

                    String venue = "";
                    String cityName = "";
                    JsonNode venues = event.path("_embedded").path("venues");
                    if (venues.isArray() && !venues.isEmpty()) {
                        JsonNode firstVenue = venues.get(0);
                        venue = firstVenue.path("name").asText("");
                        cityName = firstVenue.path("city").path("name").asText("");
                    }

                    String ticketUrl = event.path("url").asText("");
                    String status = event.path("dates").path("status").path("code").asText("");
                    concerts.add(new EventDTO(
                            id,
                            title,
                            venue,
                            cityName,
                            date,
                            time,
                            imageUrl,
                            ticketUrl,
                            status
                    ));
                }
            }

        } catch (Exception e) {
            log.error("Failed to parse Ticketmaster response", e);
            throw new RuntimeException("Failed to parse Ticketmaster response", e);
        }
        log.debug("Parsed {} events from Ticketmaster response", concerts.size());
        return concerts;
    }

    @Cacheable("suggestions")
    public List<SearchSuggestionDTO> searchSuggestions(String keyword, String city, int size) {
        List<EventDTO> events = self.getEvents(keyword, city, size, null, null);

        return events.stream()
                .map(event -> new SearchSuggestionDTO(
                        event.id(),
                        event.title(),
                        event.venue(),
                        event.city(),
                        event.date(),
                        event.time()
                ))
                .toList();
    }

}