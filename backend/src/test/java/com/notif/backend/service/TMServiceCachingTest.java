package com.notif.backend.service;

import com.notif.backend.helper.TMQueryBuilder;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.web.client.RestTemplate;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SpringJUnitConfig
@TestPropertySource(properties = {
        "ticketmaster.api.base-url=http://test",
        "ticketmaster.api.key=testkey"
})
class TMServiceCachingTest {

    @Configuration
    @EnableCaching
    static class TestConfig {

        @Bean
        CacheManager cacheManager() {
            // Simple in-memory cache instead of Redis — no Redis needed for this test
            return new ConcurrentMapCacheManager("events", "suggestions");
        }

        @Bean
        TMQueryBuilder tmQueryBuilder() {
            TMQueryBuilder q = mock(TMQueryBuilder.class);
            when(q.buildSearchUrl(any(), any(), any(), any(), anyInt(), any(), any()))
                    .thenReturn("http://test/events");
            return q;
        }

        @Bean
        RestTemplate restTemplate() {
            return mock(RestTemplate.class);
        }

        @Bean
        TMService tmService(TMQueryBuilder q, RestTemplate rt) {
            return new TMService(q, rt);
        }
    }

    @Autowired TMService     tmService;
    @Autowired RestTemplate  restTemplate;

    @Test
    void getEvents_cachesResultForIdenticalArguments() {
        // Arrange
        when(restTemplate.getForObject(anyString(), eq(String.class)))
                .thenReturn("""
                        {"_embedded":{"events":[]}}
                        """);

        // Act — two identical calls
        tmService.getEvents("metallica", null, 3, null, null);
        tmService.getEvents("metallica", null, 3, null, null);

        // Assert — HTTP call must have happened only once; second request served from cache
        verify(restTemplate, times(1)).getForObject(anyString(), eq(String.class));
    }

    @Test
    void getEvents_callsApiAgainForDifferentArguments() {
        // Arrange
        when(restTemplate.getForObject(anyString(), eq(String.class)))
                .thenReturn("""
                        {"_embedded":{"events":[]}}
                        """);

        // Act — different keywords produce different cache keys
        tmService.getEvents("metallica", null, 3, null, null);
        tmService.getEvents("slipknot",  null, 3, null, null);

        // Assert — two distinct cache keys -> two real HTTP calls
        verify(restTemplate, times(2)).getForObject(anyString(), eq(String.class));
    }
}
