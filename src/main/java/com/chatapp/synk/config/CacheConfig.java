package com.chatapp.synk.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Configuration
@EnableCaching
public class CacheConfig {

    public static final String USERCACHE = "usercache";

    @Bean
    public CacheManager cacheManager() {
        SimpleCacheManager cacheManager = new SimpleCacheManager();
        List<ConcurrentMapCache> cacheNames = new ArrayList<ConcurrentMapCache>();
        cacheNames.add(new ConcurrentMapCache(USERCACHE));
        cacheManager.setCaches(cacheNames);
        return cacheManager;
    }
}
