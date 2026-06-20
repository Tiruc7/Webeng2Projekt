package com.notif.backend.service;

import com.notif.backend.dto.EventDTO;
import com.notif.backend.helper.TMQueryBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestTemplate;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
// Mockito matchers (any(), eq()) are unannotated generics from a third-party library,
// so the null checker can't prove they satisfy RestTemplate's @NonNull parameters.
@SuppressWarnings("null")
class TMServiceTest {

    @Mock TMQueryBuilder tmQueryBuilder;
    @Mock RestTemplate   restTemplate;

    private TMService service;

    // Full Ticketmaster-like JSON with all fields populated
    private static final String FULL_JSON = """
            {
              "_embedded": {
                "events": [
                  {
                    "id": "evt1",
                    "name": "Metallica Live",
                    "url": "https://tm.example/evt1",
                    "images": [ { "url": "https://img.example/1.jpg" } ],
                    "dates": {
                      "start": { "localDate": "2026-08-14", "localTime": "20:00:00" },
                      "status": { "code": "onsale" }
                    },
                    "_embedded": {
                      "venues": [ { "name": "Olympiastadion", "city": { "name": "Berlin" } } ]
                    }
                  }
                ]
              }
            }
            """;

    @BeforeEach
    void setup() {
        // self-reference only used by searchSuggestions(), not exercised in this test class
        service = new TMService(tmQueryBuilder, restTemplate, null);
        // URL value is irrelevant; lenient so tests that don't use the builder don't fail
        lenient().when(tmQueryBuilder.buildSearchUrl(any(), any(), any(), any(), anyInt(), any(), any()))
                 .thenReturn("http://test");
    }

    @Test
    void getEvents_parsesAllFieldsOfAnEvent() {
        // Arrange
        when(restTemplate.getForObject(anyString(), eq(String.class))).thenReturn(FULL_JSON);

        // Act
        List<EventDTO> result = service.getEvents("metallica", null, 3, null, null);

        // Assert — all DTO fields must be populated from the JSON
        assertThat(result).hasSize(1);
        EventDTO e = result.get(0);
        assertThat(e.id()).isEqualTo("evt1");
        assertThat(e.title()).isEqualTo("Metallica Live");
        assertThat(e.venue()).isEqualTo("Olympiastadion");
        assertThat(e.city()).isEqualTo("Berlin");
        assertThat(e.date()).isEqualTo("2026-08-14");
        assertThat(e.time()).isEqualTo("20:00:00");
        assertThat(e.imageUrl()).isEqualTo("https://img.example/1.jpg");
        assertThat(e.ticketUrl()).isEqualTo("https://tm.example/evt1");
        assertThat(e.status()).isEqualTo("onsale");
    }

    @Test
    void getEvents_usesEmptyDefaultsForMissingFields() {
        // Arrange — minimal event with only id and name, no dates/images/venues
        String json = """
                { "_embedded": { "events": [ { "id": "evt2", "name": "No Details Show" } ] } }
                """;
        when(restTemplate.getForObject(anyString(), eq(String.class))).thenReturn(json);

        // Act
        List<EventDTO> result = service.getEvents("x", null, 3, null, null);

        // Assert — missing fields must default to empty string, never null
        assertThat(result).hasSize(1);
        EventDTO e = result.get(0);
        assertThat(e.id()).isEqualTo("evt2");
        assertThat(e.title()).isEqualTo("No Details Show");
        assertThat(e.venue()).isEqualTo("");
        assertThat(e.city()).isEqualTo("");
        assertThat(e.date()).isEqualTo("");
        assertThat(e.time()).isEqualTo("");
        assertThat(e.imageUrl()).isEqualTo("");
        assertThat(e.ticketUrl()).isEqualTo("");
        assertThat(e.status()).isEqualTo("");
    }

    @Test
    void getEvents_returnsEmptyListWhenNoEvents() {
        // Arrange — response without _embedded means no results
        when(restTemplate.getForObject(anyString(), eq(String.class))).thenReturn("{}");

        // Act
        List<EventDTO> result = service.getEvents("x", null, 3, null, null);

        // Assert
        assertThat(result).isEmpty();
    }

    @Test
    void getEvents_throwsWhenResponseIsMalformed() {
        // Arrange — not valid JSON
        when(restTemplate.getForObject(anyString(), eq(String.class)))
                .thenReturn("das ist kein json {");

        // Act + Assert
        assertThatThrownBy(() -> service.getEvents("x", null, 3, null, null))
                .isInstanceOf(RuntimeException.class);
    }
}
