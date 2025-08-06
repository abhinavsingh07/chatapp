package com.chatapp.synk.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

/**
 * Configuration class for setting up Redis caching.
 * This class defines the cache manager and the default cache configuration
 * for the application using Redis as the caching provider.
 * It enables caching support and configures the cache
 * manager to use a JSON serializer for cache values.
 * The cache entries will have a time-to-live (TTL) of 5 minutes,
 * and null values will not be cached.
 */
@Configuration
@EnableCaching
public class CacheConfig {

    /**
     * Creates a CacheManager bean that uses Redis as the caching provider.
     *
     * @param connectionFactory the RedisConnectionFactory to use for creating the CacheManager.
     * @return a CacheManager configured with RedisCacheManager.
     */
    @Bean
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        return RedisCacheManager.builder(connectionFactory).cacheDefaults(redisCacheConfiguration())//after defining the default cache configuration.redisCacheConfiguration is our bean method
                .build();
    }

    /**
     * Creates the default cache configuration for Redis.
     *
     * @return a RedisCacheConfiguration with a TTL of 5 minutes,
     */
    @Bean
    public RedisCacheConfiguration redisCacheConfiguration() {
        return RedisCacheConfiguration
                .defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(5)) // TTL for cache entries
                .disableCachingNullValues()
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer()));
    }

    /**
     * Why You Should Define It
     * By default, Spring Boot autoconfigures a RedisTemplate<String, Object> if you have Redis on your classpath.
     * However, the default serialization it uses is JDK serialization, which:
     * Is not human-readable
     * May throw exceptions if types are not Serializable
     * Is not compatible with GenericJackson2JsonRedisSerializer you’re using in your cache
     * So, to align your RedisTemplate with your cache settings, you should define:
     *
     * @param connectionFactory the RedisConnectionFactory to use for creating the RedisTemplate.
     * @return a RedisTemplate configured with String keys and Object values,
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // Use same serializer as cache for consistency
        GenericJackson2JsonRedisSerializer serializer = new GenericJackson2JsonRedisSerializer();

        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(serializer);
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(serializer);

        template.afterPropertiesSet();
        return template;
    }

}
