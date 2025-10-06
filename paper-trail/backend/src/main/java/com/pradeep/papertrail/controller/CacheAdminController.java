package com.pradeep.papertrail.controller;

import com.pradeep.papertrail.aspect.CacheMetricsAspect;
import com.pradeep.papertrail.service.CacheMonitoringService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/admin/cache")
public class CacheAdminController {

    private final CacheMonitoringService cacheMonitoringService;
    private final CacheMetricsAspect cacheMetricsAspect;

    public CacheAdminController(CacheMonitoringService cacheMonitoringService,
                                CacheMetricsAspect cacheMetricsAspect) {
        this.cacheMonitoringService = cacheMonitoringService;
        this.cacheMetricsAspect = cacheMetricsAspect;
    }

    /**
     * Get cache statistics from Redis
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getCacheStats() {
        return ResponseEntity.ok(cacheMonitoringService.getCacheStats());
    }

    /**
     * Get performance metrics (hit rate, execution times)
     */
    @GetMapping("/metrics")
    public ResponseEntity<ConcurrentHashMap<String, Object>> getCacheMetrics() {
        return ResponseEntity.ok(cacheMetricsAspect.getAllMetrics());
    }

    /**
     * Get combined stats and metrics
     */
    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> getDashboard() {
        Map<String, Object> dashboard = new java.util.HashMap<>();
        dashboard.put("redis_stats", cacheMonitoringService.getCacheStats());
        dashboard.put("performance_metrics", cacheMetricsAspect.getAllMetrics());
        return ResponseEntity.ok(dashboard);
    }

    /**
     * Reset performance metrics
     */
    @PostMapping("/metrics/reset")
    public ResponseEntity<String> resetMetrics() {
        cacheMetricsAspect.resetMetrics();
        return ResponseEntity.ok("Metrics reset successfully");
    }

    /**
     * Clear specific cache
     */
    @DeleteMapping("/{cacheName}")
    public ResponseEntity<String> clearCache(@PathVariable String cacheName) {
        cacheMonitoringService.clearCache(cacheName);
        return ResponseEntity.ok("Cache '" + cacheName + "' cleared successfully");
    }

    /**
     * Clear all caches
     */
    @DeleteMapping("/all")
    public ResponseEntity<String> clearAllCaches() {
        cacheMonitoringService.clearAllCaches();
        return ResponseEntity.ok("All caches cleared successfully");
    }

    /**
     * Warm up cache for user
     */
    @PostMapping("/warmup/{userId}")
    public ResponseEntity<String> warmUpCache(@PathVariable Long userId) {
        cacheMonitoringService.warmUpUserCache(userId);
        return ResponseEntity.ok("Cache warmed up for user: " + userId);
    }
}