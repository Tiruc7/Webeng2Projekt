package com.notif.backend.config;

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
        // Default constructor activates default typing for every type (including final
        // classes/records like EventDTO) and stores it as an "@class" JSON property,
        GenericJackson2JsonRedisSerializer serializer = new GenericJackson2JsonRedisSerializer();

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
