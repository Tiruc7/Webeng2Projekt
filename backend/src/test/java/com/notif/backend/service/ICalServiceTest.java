package com.notif.backend.service;

import com.notif.backend.dto.EventDTO;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ICalServiceTest {

    private final ICalService service = new ICalService();

    private EventDTO event(String title, String time) {
        return new EventDTO("evt1", title, "Olympiastadion", "Berlin",
                "2026-08-14", time, "", "https://tickets.example.com", "onsale");
    }

    @Test
    void generate_wrapsEventsInValidCalendar() {
        // Arrange
        List<EventDTO> events = List.of(event("Metallica Live", "20:00"));

        // Act
        String result = service.generate(events);

        // Assert
        assertThat(result).contains("BEGIN:VCALENDAR");
        assertThat(result).contains("END:VCALENDAR");
        assertThat(result).contains("BEGIN:VEVENT");
        assertThat(result).contains("END:VEVENT");
        assertThat(result).contains("SUMMARY:Metallica Live");
    }

    @Test
    void generate_usesAllDayForEventWithoutTime() {
        // Arrange
        List<EventDTO> events = List.of(event("Metallica Live", ""));

        // Act
        String result = service.generate(events);

        // Assert — all-day events use VALUE=DATE instead of a datetime stamp
        assertThat(result).contains("VALUE=DATE:");
    }

    @Test
    void generate_escapesSpecialCharacters() {
        // Arrange — title contains comma and semicolon which must be escaped per RFC 5545
        List<EventDTO> events = List.of(event("Rock, Pop; Show", "20:00"));

        // Act
        String result = service.generate(events);

        // Assert
        assertThat(result).contains("\\,");
        assertThat(result).contains("\\;");
    }

    @Test
    void generate_emptyListProducesEmptyButValidCalendar() {
        // Arrange
        List<EventDTO> events = List.of();

        // Act
        String result = service.generate(events);

        // Assert
        assertThat(result).contains("BEGIN:VCALENDAR");
        assertThat(result).contains("END:VCALENDAR");
        assertThat(result).doesNotContain("BEGIN:VEVENT");
    }
}
