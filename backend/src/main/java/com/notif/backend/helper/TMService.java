package com.notif.backend.helper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.notif.backend.dto.EventDTO;

import java.util.ArrayList;
import java.util.List;

import com.notif.backend.dto.SearchSuggestionDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class TMService {
    @Value("${ticketmaster.api.base-url}")
    private String baseUrl;

    @Value("${ticketmaster.api.key}")
    private String apiKey;

    //Objekt was JSON lesen kann
    private final ObjectMapper objectMapper = new ObjectMapper();

    private final TMQueryBuilder tmQueryBuilder;

    private final RestTemplate restTemplate = new RestTemplate();

    public TMService(TMQueryBuilder tmQueryBuilder) {
        this.tmQueryBuilder = tmQueryBuilder;
    }

    public String getRawEvents(String keyword, String city, int size) {
        String url = tmQueryBuilder.buildSearchUrl(baseUrl, apiKey, keyword, city, size);
        System.out.println("Ticketmaster URL: " + url);
        return restTemplate.getForObject(url, String.class);
    }

    public List<EventDTO> getEvents(String keyword, String city, int size) {
        String url = tmQueryBuilder.buildSearchUrl(baseUrl, apiKey, keyword, city, size);
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

                    String ticketUrl = event.path("ticketUrl").asText("");
                    String status = event.path("status").asText("");
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
            throw new RuntimeException("Failed to parse Ticketmaster response", e);
        }
        return concerts;
    }

    public List<SearchSuggestionDTO> searchSuggestions(String keyword, String city, int size) {
        List<EventDTO> events = getEvents(keyword, city, size);

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