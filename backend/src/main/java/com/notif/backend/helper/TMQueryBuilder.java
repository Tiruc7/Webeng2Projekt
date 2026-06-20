package com.notif.backend.helper;

import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

@Component
public class TMQueryBuilder {

    @NonNull
    public String buildSearchUrl(String baseUrl, String apiKey, String keyword, String city, int size) {
        return buildSearchUrl(baseUrl, apiKey, keyword, city, size, null, null);
    }

    @NonNull
    public String buildSearchUrl(String baseUrl, String apiKey, String keyword, String city, int size,
                                  String dateFrom, String dateTo) {
        StringBuilder url = new StringBuilder();

        url.append(baseUrl)
                .append("/events.json?apikey=")
                .append(encode(apiKey))
                .append("&classificationName=music");

        if (keyword != null && !keyword.isBlank()) {
            url.append("&keyword=").append(encode(keyword));
        }

        if (city != null && !city.isBlank()) {
            url.append("&city=").append(encode(city));
        }

        if (size > 0) {
            url.append("&size=").append(size);
        }

        if (dateFrom != null && !dateFrom.isBlank()) {
            url.append("&startDateTime=").append(dateFrom).append("T00:00:00Z");
        }

        if (dateTo != null && !dateTo.isBlank()) {
            url.append("&endDateTime=").append(dateTo).append("T23:59:59Z");
        }

        return Objects.requireNonNull(url.toString());
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}