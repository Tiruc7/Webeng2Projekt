package com.notif.backend.service;

import com.notif.backend.dto.EventDTO;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class ICalService {

    // iCalendar line endings must be CRLF per RFC 5545
    private static final String CRLF = "\r\n";
    private static final DateTimeFormatter DATE_FLAT = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final DateTimeFormatter DTSTAMP_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'");

    public String generate(List<EventDTO> events) {
        StringBuilder sb = new StringBuilder();

        sb.append("BEGIN:VCALENDAR").append(CRLF);
        sb.append("VERSION:2.0").append(CRLF);
        sb.append("PRODID:-//ConcertOrganizer//Webeng2Projekt//EN").append(CRLF);
        sb.append("CALSCALE:GREGORIAN").append(CRLF);
        sb.append("METHOD:PUBLISH").append(CRLF);

        String nowStamp = ZonedDateTime.now(ZoneOffset.UTC).format(DTSTAMP_FORMAT);

        for (EventDTO event : events) {
            sb.append(buildEvent(event, nowStamp));
        }

        sb.append("END:VCALENDAR").append(CRLF);
        return sb.toString();
    }

    private String buildEvent(EventDTO event, String nowStamp) {
        StringBuilder sb = new StringBuilder();

        String dateFlat = event.date().replace("-", "");
        String[] times = resolveTimes(event, dateFlat);
        String dtStart = times[0];
        String dtEnd   = times[1];

        String location = resolveLocation(event);

        sb.append("BEGIN:VEVENT").append(CRLF);
        sb.append("UID:").append(event.id()).append("@concertorganizer").append(CRLF);
        sb.append("DTSTAMP:").append(nowStamp).append(CRLF);
        sb.append("DTSTART:").append(dtStart).append(CRLF);
        sb.append("DTEND:").append(dtEnd).append(CRLF);
        sb.append("SUMMARY:").append(escapeText(event.title())).append(CRLF);

        if (!location.isBlank()) {
            sb.append("LOCATION:").append(escapeText(location)).append(CRLF);
        }
        if (event.ticketUrl() != null && !event.ticketUrl().isBlank()) {
            sb.append("URL:").append(event.ticketUrl()).append(CRLF);
        }

        sb.append("END:VEVENT").append(CRLF);
        return sb.toString();
    }

    // Returns [dtStart, dtEnd]. If a time is known, produce a UTC datetime with +3h duration.
    // Otherwise fall back to all-day (DATE-only) values spanning one calendar day.
    private String[] resolveTimes(EventDTO event, String dateFlat) {
        if (event.time() != null && !event.time().isBlank()) {
            try {
                String[] parts = event.time().split(":");
                int hour   = Integer.parseInt(parts[0]);
                int minute = Integer.parseInt(parts[1]);

                // Use LocalDateTime.plusHours so dates roll over correctly (e.g. 22:00 + 3h = 01:00 next day)
                LocalDate date = LocalDate.parse(event.date());
                LocalDateTime startDt = LocalDateTime.of(date, java.time.LocalTime.of(hour, minute));
                LocalDateTime endDt   = startDt.plusHours(3);

                String dtStart = startDt.format(DTSTAMP_FORMAT);
                String dtEnd   = endDt.format(DTSTAMP_FORMAT);
                return new String[]{dtStart, dtEnd};
            } catch (Exception ignored) {
            }
        }

        // All-day event: DTEND is the next calendar day (exclusive, per RFC 5545)
        String dtStart = "VALUE=DATE:" + dateFlat;
        String dtEnd;
        try {
            dtEnd = "VALUE=DATE:" + LocalDate.parse(event.date()).plusDays(1).format(DATE_FLAT);
        } catch (Exception e) {
            dtEnd = "VALUE=DATE:" + dateFlat;
        }
        return new String[]{dtStart, dtEnd};
    }

    private String resolveLocation(EventDTO event) {
        String venue = event.venue();
        String city  = event.city();
        boolean hasVenue = venue != null && !venue.isBlank();
        boolean hasCity  = city  != null && !city.isBlank();

        if (hasVenue && hasCity) return venue + ", " + city;
        if (hasVenue)            return venue;
        if (hasCity)             return city;
        return "";
    }

    private String escapeText(String text) {
        if (text == null) return "";
        return text
                .replace("\\", "\\\\")
                .replace(";",  "\\;")
                .replace(",",  "\\,")
                .replace("\n", "\\n")
                .replace("\r", "");
    }
}
