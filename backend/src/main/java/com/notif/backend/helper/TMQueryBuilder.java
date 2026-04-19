package com.notif.backend.helper;

import org.springframework.stereotype.Component;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Component
public class TMQueryBuilder {

    public String buildSearchUrl(String baseUrl, String apiKey, String keyword, String city, int size) {
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

        return url.toString();
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}