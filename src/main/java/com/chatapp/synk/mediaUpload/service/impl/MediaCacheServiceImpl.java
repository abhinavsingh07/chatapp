package com.chatapp.synk.mediaUpload.service.impl;

import com.chatapp.synk.mediaUpload.service.MediaCacheService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

@Service
public class MediaCacheServiceImpl implements MediaCacheService {

    private static final Logger logger = LoggerFactory.getLogger(MediaCacheServiceImpl.class);

    private final CacheManager cacheManager;

    public MediaCacheServiceImpl(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    @Override
    public void evictProfilePictureCaches(Long userId) {
        Cache userCache = cacheManager.getCache("userCache");
        if (userCache != null) {
            userCache.evict(userId);
            logger.debug("Evicted userCache for userId: {} after profile picture upload", userId);
        }

        Cache contactListCache = cacheManager.getCache("contactListCache");
        if (contactListCache != null) {
            contactListCache.evict(userId);
            logger.debug("Evicted contactListCache for userId: {} after profile picture upload", userId);
        }
    }
}
