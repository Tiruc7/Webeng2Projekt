package com.notif.backend.config;

import com.notif.backend.dto.EventDTO;
import com.notif.backend.dto.SearchSuggestionDTO;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.serializer.RedisSerializationContext.SerializationPair;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;

class CacheConfigTest {

    private final SerializationPair<Object> serializationPair =
            new CacheConfig().defaultCacheConfig().getValueSerializationPair();

    @Test
    void cachedEventListSurvivesRoundTrip() {
        List<EventDTO> events = List.of(new EventDTO("1", "t", "v", "c", "d", "ti", "img", "tu", "s"));

        // Spring's RedisCache reads every cached value back through this exact read/write
        // pair on a real cache hit - that is the path that broke before the WRAPPER_ARRAY fix.
        ByteBuffer bytes = serializationPair.write(Objects.requireNonNull(events));
        Object back = serializationPair.read(bytes);

        assertThat(back).isEqualTo(events);
    }

    @Test
    void cachedSuggestionListSurvivesRoundTrip() {
        List<SearchSuggestionDTO> suggestions = List.of(new SearchSuggestionDTO("1", "t", "v", "c", "d", "ti"));

        ByteBuffer bytes = serializationPair.write(Objects.requireNonNull(suggestions));
        Object back = Objects.requireNonNull(serializationPair.read(bytes));

        assertThat(back).isInstanceOf(List.class);
        SearchSuggestionDTO backFirst = (SearchSuggestionDTO) ((List<?>) back).get(0);
        assertThat(backFirst.getId()).isEqualTo("1");
        assertThat(backFirst.getTitle()).isEqualTo("t");
    }
}
