package com.pradeep.papertrail.service;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Service
public class CacheMonitoringService {

    private final RedisTemplate<String, Object> redisTemplate;

    public CacheMonitoringService(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * Get cache statistics
     */
    public Map<String, Object> getCacheStats() {
        Map<String, Object> stats = new HashMap<>();

        stats.put("note_cache_size", getCacheSize("note::*"));
        stats.put("userNotes_cache_size", getCacheSize("userNotes::*"));
        stats.put("sharedNotes_cache_size", getCacheSize("sharedNotes::*"));
        stats.put("total_keys", getTotalKeys());

        return stats;
    }

    /**
     * Clear specific cache
     */
    public void clearCache(String cacheName) {
        Set<String> keys = redisTemplate.keys(cacheName + "::*");
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
    }

    /**
     * Clear all caches
     */
    public void clearAllCaches() {
        clearCache("note");
        clearCache("userNotes");
        clearCache("sharedNotes");
    }

    /**
     * Warm up cache for a specific user
     */
    public void warmUpUserCache(Long userId) {
        // This would be called by the service layer after user login
        // to pre-populate frequently accessed data
    }

    private long getCacheSize(String pattern) {
        Set<String> keys = redisTemplate.keys(pattern);
        return keys != null ? keys.size() : 0;
    }

    private long getTotalKeys() {
        Set<String> keys = redisTemplate.keys("*");
        return keys != null ? keys.size() : 0;
    }
}