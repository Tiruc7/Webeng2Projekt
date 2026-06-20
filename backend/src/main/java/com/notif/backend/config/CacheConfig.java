package com.notif.backend.config;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.lang.NonNull;

import java.time.Duration;
import java.util.Objects;

@Configuration
@EnableCaching
public class CacheConfig {

    @NonNull
    private static final Duration TTL = Objects.requireNonNull(Duration.ofDays(5));

    @Bean
    @NonNull
    public RedisCacheConfiguration defaultCacheConfig() {
        // WRAPPER_ARRAY (instead of the default PROPERTY) typing is required because cached
        // values are Lists (e.g. List<EventDTO>): PROPERTY can only attach a type id inside a
        // JSON object, not on a bare JSON array root, so reading a cached list back. WRAPPER_ARRAY wraps any value, including array roots.
        @SuppressWarnings("deprecation")
        ObjectMapper.DefaultTyping typing = ObjectMapper.DefaultTyping.EVERYTHING;

        ObjectMapper mapper = new ObjectMapper();
        mapper.activateDefaultTyping(
                BasicPolymorphicTypeValidator.builder().allowIfSubType(Object.class).build(),
                typing,
                JsonTypeInfo.As.WRAPPER_ARRAY);
        GenericJackson2JsonRedisSerializer serializer = new GenericJackson2JsonRedisSerializer(mapper);

        return RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(TTL)
                .serializeValuesWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(serializer)
                );
    }

    @Bean
    public RedisCacheManager cacheManager(@NonNull RedisConnectionFactory factory) {
        return RedisCacheManager.builder(factory)
                .cacheDefaults(defaultCacheConfig())
                .build();
    }
}
